package eu.sqooss.service.abstractmetric;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.scheduler.Job;
import eu.sqooss.service.util.Pair;

public class DefaultMeasurementEvaluation implements MetricMeasurementEvaluation{
	Logger log = AlitheiaCore.getInstance().getLogManager().createLogger(Logger.NAME_SQOOSS_METRIC);
	
	/**
     * The scheduler job that executes this metric. 
     */
    protected ThreadLocal<Job> job = new ThreadLocal<Job>();
    
    /** Set of declared metrics indexed by their mnemonic*/
    private Map<String, Metric> metrics = new HashMap<String, Metric>();
	
	private AlitheiaPlugin plugin;
	private MetricDiscovery metricDiscovery;
	
	public DefaultMeasurementEvaluation(AlitheiaPlugin plugin, MetricDiscovery metricDiscovery) {
		this.plugin = plugin;
		this.metricDiscovery = metricDiscovery;
	}
	
	
	/**
     * Call the appropriate getResult() method according to
     * the type of the entity that is measured.
     *
     * Use this method if you don't want the metric results
     * to be calculated on-demand. Otherwise, use getResult().
     *
     * @param o DAO that specifies the desired result type.
     *      The type of o is used to dispatch to the correct
     *      specialized getResult() method of the sub-interfaces.
     * @return result (measurement) performed by this metric
     *      on the project data specified by o.
     * @throws MetricMismatchException if the DAO is of a type
     *      not supported by this metric.
     */
     @SuppressWarnings("unchecked")
     public List<Result> getResultIfAlreadyCalculated(DAObject o, List<Metric> l) throws MetricMismatchException {
        boolean found = false;        
        List<Result> result = new ArrayList<Result>();
        
        for (Metric m : l) {
            if (!metrics.containsKey(m.getMnemonic())) {
                throw new MetricMismatchException("Metric " + m.getMnemonic()
                        + " not defined by plugin "
                        + Plugin.getPluginByHashcode(plugin.getUniqueKey()).getName());
            }
            List<Result> re = null;
            try {
                Method method = findGetResultMethod(o.getClass());
                re = (List<Result>) method.invoke(this, o, m);
            } catch (SecurityException e) {
                logErr("getResult", o, e);
            } catch (NoSuchMethodException e) {
                log.error("No method getResult(" + m.getMetricType().toActivator() + ") for type "
                        + this.getClass().getName());
            } catch (IllegalArgumentException e) {
                logErr("getResult", o, e);
            } catch (IllegalAccessException e) {
                logErr("getResult", o, e);
            } catch (InvocationTargetException e) {
                logErr("getResult", o, e);
            }
            if (re != null && !re.isEmpty()) {
                result.addAll(re);
            }
        }

        return result;
    }

     private Method findGetResultMethod(Class<?> clazz) 
     throws NoSuchMethodException {
     Method m = null;
     
     try {
         m = this.getClass().getMethod("getResult", clazz, Metric.class);                
     } catch (NoSuchMethodException nsme) {
         try {
             m = this.getClass().getMethod("getResult", clazz.getSuperclass(), Metric.class);
         } catch (NoSuchMethodException nsme1) {
             throw nsme;
         }
     }

     return m;
     }
     
    /**
     * Call the appropriate getResult() method according to
     * the type of the entity that is measured.
     *
     * If the appropriate getResult() doesn't return any value,
     * the metric is forced to calculate the result. Then the
     * appropriate getResult() method is called again.
     *
     * @param o DAO that specifies the desired result type.
     *      The type of o is used to dispatch to the correct
     *      specialized getResult() method of the sub-interfaces.
     * @return result (measurement) performed by this metric
     *      on the project data specified by o.
     * @throws MetricMismatchException if the DAO is of a type
     *      not supported by this metric.
     * @throws AlreadyProcessingException 
     */
    public List<Result> getResult(DAObject o, List<Metric> l) 
    throws MetricMismatchException, AlreadyProcessingException, Exception {
        List<Result> r = getResultIfAlreadyCalculated(o, l);

        // the result hasn't been calculated yet. Do so.
        if (r == null || r.size() == 0) {
           /*
             * To ensure that no two instances of the metric operate on the same
             * DAO lock on the DAO. Working on the same DAO can happen often
             * when a plugin starts the calculation of another metric as a
             * result of a plugin dependency association. This lock has the side
             * effect that no two Plugins can be invoked with the same DAO as an
             * argument even if the plug-ins do not depend on each other.
             */
            synchronized (lockObject(o)) {
                try {
                    run(o);
                    
                    r = getResultIfAlreadyCalculated(o, l);
                    if (r == null || r.size() == 0) {
                        if (job.get().state() != Job.State.Yielded)
                            log.debug("Metric " + getClass() + " didn't return"
                                + "a result even after running it. DAO: "
                                + o.getId());
                    }
                } finally {
                    unlockObject(o);
                }
            }
        }

        return r;
    }

    private Map<Long,Pair<Object,Integer>> locks = new HashMap<Long,Pair<Object,Integer>>();
    
    private Object lockObject(DAObject o) throws AlreadyProcessingException {
    	synchronized (locks) {
            if (!locks.containsKey(o.getId())) {
                locks.put(o.getId(), 
                        new Pair<Object, Integer>(new Object(),0));
            }
            Pair<Object, Integer> p = locks.get(o.getId());
            if (p.second + 1 > 1) {
                /*
                 * Break and reschedule the calculation of each call to the
                 * getResult method if it originates from another thread than
                 * the thread that has currently locked the DAO object. 
                 * This is required for the DB transaction in the stopped
                 * job to see the results of the calculation of the original
                 * job.
                 */ 
                log.debug("DAO Id:" + o.getId() + 
                        " Already locked - failing job");
                try {
                    throw new AlreadyProcessingException();
                } finally {
                    MetricActivator ma = AlitheiaCore.getInstance().getMetricActivator();
                    ma.runMetric(o, plugin);
                }
            }
            p.second = p.second + 1;
            return p.first;
        }
    }
    
    private void unlockObject(DAObject o) {
    	synchronized(locks) {
    		Pair<Object,Integer> p = locks.get(o.getId());
    		p.second = p.second - 1;
    		if (p.second == 0) {
    			locks.remove(o.getId());
    		} else {
    		log.debug("Unlocking DAO Id:" + o.getId());
    		}
    	}
    }
    
    /**
     * Call the appropriate run() method according to the type of the entity
     * that is measured.
     *
     * @param o
     *                DAO which determines which sub-interface run method is
     *                called and also determines what is to be measured by that
     *                sub-interface.
     * @throws MetricMismatchException
     *                 if the DAO is of a type not supported by this metric.
     */
    public void run(DAObject o) throws MetricMismatchException, 
        AlreadyProcessingException, Exception {

        if (!metricDiscovery.checkDependencies()) {
            log.error("Plug-in dependency check failed");
            return;
        }

        try {
            Method m = findRunMethod("run", o.getClass());
            m.invoke(this, o);
        } catch (SecurityException e) {
            logErr("run", o, e);
        } catch (NoSuchMethodException e) {
            logErr("run", o, e);
        } catch (IllegalArgumentException e) {
            logErr("run", o, e);
        } catch (IllegalAccessException e) {
            logErr("run", o, e);
        } catch (InvocationTargetException e) {
            // Forward exception to metric job exception handler
            if (e.getCause() instanceof AlreadyProcessingException) {
                throw (AlreadyProcessingException) e.getCause();
            } else {
                if (e != null && e.getCause() != null) {
                    logErr("run", o, e);
                    if (e.getCause() != null)
                        throw new Exception(e.getCause());
                    else
                        throw new Exception(e);
                }
            }
        }
    }
    
    
    
    private Method findRunMethod(String name, Class<?> clazz) 
            throws NoSuchMethodException {
            Method m = null;
            
            try {
                m = this.getClass().getMethod(name, clazz);                
            } catch (NoSuchMethodException nsme) {
                try {
                    m = this.getClass().getMethod(name, clazz.getSuperclass());
                } catch (NoSuchMethodException nsme1) {
                    throw nsme;
                }
            }
           
            return m;
        }
    
    private void logErr(String method, DAObject o, Exception e) {
        log.error("Plugin:" + this.getClass().toString() + 
                "\nDAO id: " + o.getId() + 
                "\nDAO class: " + o.getClass() +
                "\nDAO toString(): " + o.toString() +
                "\nError when invoking the " + method + " method." +
                "\nException:" + e.getClass().getName() +
                "\nError:" + e.getMessage() + 
                "\nReason:" + e.getCause(), e);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setJob(Job j) {
        this.job.set(j);
    }
}
