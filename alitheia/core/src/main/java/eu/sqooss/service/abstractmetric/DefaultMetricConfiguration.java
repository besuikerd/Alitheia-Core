package eu.sqooss.service.abstractmetric;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.db.PluginConfiguration;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.db.MetricType.Type;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivationException;
import eu.sqooss.service.pa.PluginInfo;

public class DefaultMetricConfiguration implements MetricConfiguration{
	protected static final String QRY_SYNC_PV = "select pv.id from ProjectVersion pv " +
    		"where pv.project = :project and not exists(" +
    		"	select pvm.projectVersion from ProjectVersionMeasurement pvm " +
    		"	where pvm.projectVersion.id = pv.id and pvm.metric.id = :metric) " +
    		"order by pv.sequence asc";
    
    protected static final String QRY_SYNC_PF = "select pf.id " +
    		"from ProjectVersion pv, ProjectFile pf " +
    		"where pf.projectVersion=pv and pv.project = :project " +
    		"and not exists (" +
    		"	select pfm.projectFile " +
    		"	from ProjectFileMeasurement pfm " +
    		"	where pfm.projectFile.id = pf.id " +
    		"	and pfm.metric.id = :metric) " +
    		"	and pf.isDirectory = false)  " +
    		"order by pv.sequence asc";
    
    protected static final String QRY_SYNC_PD = "select pf.id " +
		"from ProjectVersion pv, ProjectFile pf " +
		"where pf.projectVersion=pv and pv.project = :project " +
		"and not exists (" +
		"	select pfm.projectFile " +
		"	from ProjectFileMeasurement pfm " +
		"	where pfm.projectFile.id = pf.id " +
		"	and pfm.metric.id = :metric) " +
		"	and pf.isDirectory = true)  " +
		"order by pv.sequence asc";
    
    protected static final String QRY_SYNC_MM = "select mm.id " +
    		"from MailMessage mm " +
    		"where mm.list.storedProject = :project " +
    		"and mm.id not in (" +
    		"	select mmm.mail.id " +
    		"	from MailMessageMeasurement mmm " +
    		"	where mmm.metric.id =:metric and mmm.mail.id = mm.id))";
    
    protected static final String QRY_SYNC_MT = "select mlt.id " +
    		"from MailingListThread mlt " +
    		"where mlt.list.storedProject = :project " +
    		"and mlt.id not in (" +
    		"	select mltm.thread.id " +
    		"	from MailingListThreadMeasurement mltm " +
    		"	where mltm.metric.id =:metric and mltm.thread.id = mlt.id)";
    
    protected static final String QRY_SYNC_DEV = "select d.id " +
    		"from Developer d " +
    		"where d.storedProject = :project";
    
    protected static final String QRY_SYNC_NS = "select ns.id " +
            "from NameSpace ns, ProjectVersion pv " +
            "where pv = ns.changeVersion " +
            "and pv.project = :project " +
            "and not exists ( " +
            "   select nsm " + 
            "   from NameSpaceMeasurement nsm " + 
            "   where nsm.metric.id = :metric " +
            "   and nsm.namespace = ns) " +
            "order by pv.sequence asc";
    
    protected static final String QRY_SYNC_ENCUNT = "select encu.id " +
            "from EncapsulationUnit encu, ProjectVersion pv, ProjectFile pf " +
            " where pf.projectVersion = pv " +
            " and encu.file = pf " +
            "and pv.project = :project " +
            "and not exists ( " +
            "    select eum " +
            "    from EncapsulationUnitMeasurement eum " +
            "    where eum.encapsulationUnit = encu " +
            "    and eum.metric.id = :metric " +
            " ) order by pv.sequence asc ";
    
    protected static final String QRY_SYNC_EXECUNT = "select exu.id " +
    		"from ExecutionUnit exu, EncapsulationUnit encu, " +
    		"     ProjectVersion pv, ProjectFile pf " +
            "where pf.projectVersion = pv " +
            "and encu.file = pf " +
            "and pv.project = :project " +
            "and exu.changed = true " +
            "and exu.encapsulationUnit = encu " +
            "and not exists ( " +
            "    select eum  " +
            "    from ExecutionUnitMeasurement eum " +
            "    where eum.executionUnit = exu " +
            "    and eum.metric.id = :metric) " +
            "order by pv.sequence asc";
	
	
	Logger log = AlitheiaCore.getInstance().getLogManager().createLogger(Logger.NAME_SQOOSS_METRIC);
	
	private BundleContext bc;
	private AlitheiaPlugin plugin;
	private MetricDiscovery metricDiscovery;
	private DBService db;
	
	public DefaultMetricConfiguration(BundleContext bc, AlitheiaPlugin plugin, MetricDiscovery metricDiscovery){
		this.bc = bc;
		this.plugin = plugin;
		this.metricDiscovery = metricDiscovery;
		this.db = AlitheiaCore.getInstance().getDBService();
	}
	
	
	/**
     * Default (empty) implementation of the clean up method. What to 
     * do with the provided DAO is left to sub-classes to decide.
     * {@inheritDoc}
     */
    public boolean cleanup(DAObject sp) {
        log.warn("Empty cleanup method for plug-in " 
                + this.getClass().getName());
        return true; 
    }
    
    /**
     * Return an MD5 hex key uniquely identifying the plug-in
     */
    public final String getUniqueKey() {
    	MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			log.error("Cannot find a valid implementation of the MD5 " +
					"hash algorithm");
		}
    	String name = this.getClass().getCanonicalName();
		byte[] data = name.getBytes(); 
		m.update(data,0,data.length);
		BigInteger i = new BigInteger(1,m.digest());
		return String.format("%1$032X", i);
    }
    
    /**{@inheritDoc}*/
    public final Set<Class<? extends DAObject>> getActivationTypes() {    
        return metricDiscovery.getActivators();
    }
    
    @Override
    public Set<String> getDependencies() {
    	return metricDiscovery.getDependencies();
    }
    
    @Override
    public final List<Class<? extends DAObject>> getMetricActivationTypes (Metric m) {
        return metricDiscovery.getMetricActType().get(m);
    }
    
    /** {@inheritDoc} */
    public final Set<PluginConfiguration> getConfigurationSchema() {
        // Retrieve the plug-in's info object
        PluginInfo pi = AlitheiaCore.getInstance().getPluginAdmin().getPluginInfo(getUniqueKey());
        if (pi == null) {
            // The plug-in's info object is always null during bundle startup,
            // but if it is not available when the bundle is active, something
            // is possibly wrong.
            if (bc.getBundle().getState() == Bundle.ACTIVE) {
                log.warn("Plugin <" + plugin.getName() + "> is loaded but not installed.");
            }
            return Collections.emptySet();
        }
        return pi.getConfiguration();
    }
    

    @Override
    public Map<MetricType.Type, SortedSet<Long>> getObjectIdsToSync(StoredProject sp, Metric m) 
    throws MetricActivationException {

    	Map<MetricType.Type, SortedSet<Long>> IDs = new HashMap<Type, SortedSet<Long>>();
    	
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("project", sp);
        params.put("metric", m.getId());

    	String q = null;
    	
    	for (Class<? extends DAObject> at : getMetricActivationTypes(m)) {
    	
	    	if (MetricType.fromActivator(at) == Type.PROJECT_VERSION) {
	    		q = QRY_SYNC_PV;
	    	} else if (MetricType.fromActivator(at) == Type.SOURCE_FILE) {
	    		q = QRY_SYNC_PF;
	    	} else if (MetricType.fromActivator(at) == Type.SOURCE_DIRECTORY) {
	    		q = QRY_SYNC_PD;
	     	} else if (MetricType.fromActivator(at) == Type.MAILING_LIST) {
	    		throw new MetricActivationException("Metric synchronisation with MAILING_LIST objects not implemented");
	    	} else if (MetricType.fromActivator(at) == Type.MAILMESSAGE) {
	    		q = QRY_SYNC_MM;
	    	} else if (MetricType.fromActivator(at) == Type.MAILTHREAD) {
	    		q = QRY_SYNC_MT;
	    	} else if (MetricType.fromActivator(at) == Type.BUG) {
	    		throw new MetricActivationException("Metric synchronisation with BUG objects not implemented");
	    	} else if (MetricType.fromActivator(at) == Type.DEVELOPER) {
	    		q = QRY_SYNC_DEV;
	    	} else if (MetricType.fromActivator(at) == Type.NAMESPACE) {
                q = QRY_SYNC_NS;
            } else if (MetricType.fromActivator(at) == Type.ENCAPSUNIT) {
                q = QRY_SYNC_ENCUNT;
            } else if (MetricType.fromActivator(at) == Type.EXECUNIT) {
                q = QRY_SYNC_EXECUNT;
            } else {
	    		throw new MetricActivationException("Metric synchronisation with GENERIC objects not implemented");
	    	}
	    	
	    	List<Long> objectIds = (List<Long>) db.doHQL(q, params);
	    	TreeSet<Long> ids = new TreeSet<Long>();
	    	ids.addAll(objectIds);
	    	IDs.put(MetricType.fromActivator(at), ids);
    	}
    	return IDs;
    }
    
    public List<Metric> getAllSupportedMetrics() {
        String qry = "from Metric m where m.plugin=:plugin";
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("plugin", Plugin.getPluginByHashcode(getUniqueKey()));
        
        return (List<Metric>)db.doHQL(qry, params);
    }
    
    /** {@inheritDoc} */
    @Override
    public List<Metric> getSupportedMetrics(Class<? extends DAObject> activator) {
        List<Metric> m = new ArrayList<Metric>();

        //Query the database just once
        List<Metric> all = getAllSupportedMetrics();
        
        if (all == null || all.isEmpty())
            return m;
        
        for (Metric metric : all) {
            if (getMetricActivationTypes(metric).contains(activator)) {
                m.add(metric);
            }
        }
        
        return m;
    }
    
    
    public PluginConfiguration getConfigurationOption(String config) {
        Set<PluginConfiguration> conf = 
            AlitheiaCore.getInstance().getPluginAdmin().getPluginInfo(getUniqueKey()).getConfiguration();
        
        Iterator<PluginConfiguration> i = conf.iterator();
        
        while (i.hasNext()) {
            PluginConfiguration pc = i.next();
            if (pc.getName().equals(config)) {
                return pc;
            }
        }
        
        /* Config option not found */
        return null;
    }
}
