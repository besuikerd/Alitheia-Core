/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 - 2010 - Organization for Free and Open Source Software,  
 *                Athens, Greece.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package eu.sqooss.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import eu.sqooss.impl.service.admin.AdminServiceImpl;
import eu.sqooss.impl.service.cluster.ClusterNodeServiceImpl;
import eu.sqooss.impl.service.db.DBServiceImpl;
import eu.sqooss.impl.service.fds.FDSServiceImpl;
import eu.sqooss.impl.service.logging.LogManagerImpl;
import eu.sqooss.impl.service.metricactivator.MetricActivatorImpl;
import eu.sqooss.impl.service.pa.PAServiceImpl;
import eu.sqooss.impl.service.rest.ResteasyServiceImpl;
import eu.sqooss.impl.service.scheduler.SchedulerServiceImpl;
import eu.sqooss.impl.service.tds.TDSServiceImpl;
import eu.sqooss.impl.service.updater.UpdaterServiceImpl;
import eu.sqooss.impl.service.webadmin.WebadminServiceImpl;
import eu.sqooss.service.admin.AdminService;
import eu.sqooss.service.cluster.ClusterNodeService;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.fds.FDSService;
import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.metricactivator.MetricActivator;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.rest.RestService;
import eu.sqooss.service.scheduler.Scheduler;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.updater.UpdaterService;
import eu.sqooss.service.webadmin.WebadminService;

/**
 * Startup class of the Alitheia framework's core. Its main goal is to
 * initialize all core components and be able to provide them upon request.
 * There is one AlitheiaCore instance which may be retrieved statically
 * through getInstance(); after that you can use the get*Service() methods
 * to get each of the other core components as needed.
 */
public class AlitheiaCore {

    /** The Logger component's instance. */
    private LogManagerImpl logger;
    
    /** The parent bundle's context object. */
    private BundleContext bc;
    
    /** The Core is singleton-line because it has a special instance */
    private static AlitheiaCore instance = null;
    
    /** Holds initialised service instances */
    private Map<Class<? extends AlitheiaCoreService>, AlitheiaCoreService> instances = new HashMap<Class<? extends AlitheiaCoreService>, AlitheiaCoreService>();
    
    /** Contains the registered services in the order of initialization */
    private List<Class<? extends AlitheiaCoreService>> services = new ArrayList<Class<? extends AlitheiaCoreService>>();
    
    /** Mapping from service to the class which implements this service */
    private Map<Class<? extends AlitheiaCoreService>, Class<? extends AlitheiaCoreService>> implementations = new HashMap<Class<? extends AlitheiaCoreService>, Class<? extends AlitheiaCoreService>>();

    /** The locally stored SecurityManager instance */
	private SecurityManager securityManager;
   
    /**
     * Simple constructor.
     * 
     * @param bc The parent bundle's context object.
     */
    private AlitheiaCore() {
    }
    
    private void registerBaseServices(){
    	// Create LogManager and DBService before they are initialized
    	this.createLogManager();
    	this.createDBService();
    	
    	this.registerService(LogManager.class, LogManagerImpl.class);
    	this.registerService(DBService.class, DBServiceImpl.class);	 
    	this.registerService(PluginAdmin.class, PAServiceImpl.class);
    	this.registerService(Scheduler.class, SchedulerServiceImpl.class);
    	this.registerService(TDSService.class, TDSServiceImpl.class);
    	this.registerService(ClusterNodeService.class, ClusterNodeServiceImpl.class);
    	this.registerService(FDSService.class, FDSServiceImpl.class);
    	this.registerService(MetricActivator.class, MetricActivatorImpl.class);
    	this.registerService(UpdaterService.class, UpdaterServiceImpl.class);
    	this.registerService(WebadminService.class, WebadminServiceImpl.class);
    	this.registerService(RestService.class, ResteasyServiceImpl.class);
    	this.registerService(AdminService.class, AdminServiceImpl.class);
    }
    
    private void createLogManager(){
    	logger = new LogManagerImpl();
        logger.setInitParams(bc, null);
        if (!logger.startUp()) {
            err("Cannot start the log service, aborting");
        }
        instances.put(LogManager.class, logger);
        err("Service " + LogManagerImpl.class.getName() + " started");
    }
    
    private void createDBService() {
    	DBService db = DBServiceImpl.getInstance();
        db.setInitParams(bc, logger.createLogger("sqooss.db"));
        if (!db.startUp()) {
            err("Cannot start the DB service, aborting");
        }
        instances.put(DBService.class, db);
        err("Service " + DBServiceImpl.class.getName() + " started");
    }

    /**
     * The core has a blessed instance which you can get from here;
     * that instance in turn will give you the DB service and others
     * that it holds on to. So code that needs a particular service
     * can use AlitheiaCore.getInstance().get*Service() to get
     * a reference to specific services.
     * 
     * @return Instance, or null if it's not initialized yet
     */
    public static AlitheiaCore getInstance() {
    	if (instance == null) instance = new AlitheiaCore();
        return instance;
    }
    
    /*Create a temp instance to use for testing.*/
    public static AlitheiaCore testInstance() {
    	// Create instance
    	AlitheiaCore testInstance = AlitheiaCore.getInstance();
    	// Logger should always exist
    	testInstance.createLogManager();
    	// Return instance
    	return testInstance;
    }
    
    /**
     * Register an external implementation of an AlitheiaCore service. It
     * will override any internally defined implementation.
     *
     * @param service The service interface to register an implementation for
     * @param clazz The class that implements the registered service
     */
    public synchronized void registerService(
            Class<? extends AlitheiaCoreService> service,
            Class<? extends AlitheiaCoreService> implementation) {

    	// Add to the list of services of needed (to remember ordering)
    	if(!services.contains(service)){
    		services.add(service);
    	}
    	
    	// Get the old implementaiton
    	Class<? extends AlitheiaCoreService> old = implementations.get(service);
    	
    	// Add to implementation (or update the implementation)
        implementations.put(service, implementation);
    	
    	// Check if implementation changed
    	if(old != null && !old.equals(implementation)){
    		// New implementation: remove instance
    		instances.remove(service);
    	}
    	
    	// Initialize the service
        initService(service);
    }

    /**
     * Unregisters an external implementation of an Alitheia Core service. 
     * This method does not check if external entities hold references to the
     * service to be unregistered.
     */
    public synchronized void unregisterService(
            Class<? extends AlitheiaCoreService> service) {
        services.remove(service);
        implementations.remove(service);
    }

    /**
     * This method performs initialization of the <code>AlitheiaCore</code>
     * object by instantiating the core components, by calling the 
     * method on their service interface. Failures are reported but do not 
     * block the instatiation process).
     */
    public void init(BundleContext bc) {
    	this.bc = bc;
    	
    	registerBaseServices();

        err("Required services online, initialising");        

        for (Class<? extends AlitheiaCoreService> s : services) {
            initService(s);
        }
    }

    private synchronized void initService(Class<? extends AlitheiaCoreService> s) {
    	// Do not initialize the service again
    	if(instances.containsKey(s)) return;
    	
        Class<? extends AlitheiaCoreService> impl = implementations.get(s);

        if (impl == null) {
            err("No implementation found for service " + s);
            return;
        }

        try {
            AlitheiaCoreService o = impl.newInstance();

            if (o == null) {
                err("Service object for service " + s
                        + " could not be created");
                return;
            }

            //Extract the unique service portion of the class FQN.
            //e.g. from eu.sqooss.service.db.DBService -> db
            String[] paths = s.getCanonicalName().split("\\.");

            /* Logger names are constructed as per */
            o.setInitParams(bc,
                    logger.createLogger("sqooss." + paths[3]));

            if (!o.startUp()) {
                err("Service " + s + " could not be started");
                return;
            }

            instances.put(s, o);
            err("Service " + impl.getName() + " started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * Unused check of the core instance for liveness. Because the instance
	 * might not lee without the rest of the bikini services, we need to
	 * check that they are present.
	 * Added after evening discussion (<i>some 5 pints and a bunch of naked
	 * bikini models later<i>) at Amarilia on liveness.
	 */
	private static boolean canLee(boolean touLiBouDiBouDauTcou) {
	    return (null != instance) && touLiBouDiBouDauTcou;
	}

	public void shutDown() {
    	// Copy
    	List<Class<? extends AlitheiaCoreService>> revServices = 
    		new ArrayList<Class<? extends AlitheiaCoreService>>(services);
    	
    	// Reverse
    	Collections.reverse(services);
    	
    	for (Class<? extends AlitheiaCoreService> s : revServices) {
    		Object o = instances.get(s);
    		try	{
    			s.cast(o).shutDown();
    			instances.remove(s);
    		} catch (Throwable t) {
    			t.printStackTrace();
			}    		
    	}
	}
    
    /**
     * Returns the instance of the given service
     */
	@SuppressWarnings("unchecked")
	public <T extends AlitheiaCoreService> T getService(Class<T> service){
    	return (T) instances.get(service);
    }

    /**
     * Returns the locally stored Logger component's instance.
     * 
     * @return The Logger component's instance.
     */
    public LogManager getLogManager() {
    	return this.getService(LogManager.class);
    }

    /**
     * Returns the locally stored WebAdmin component's instance.
     * 
     * @return The WebAdmin component's instance.
     */
    public WebadminService getWebadminService() {
    	return this.getService(WebadminService.class);
    }

    /**
     * Returns the locally stored Plug-in Admin component's instance.
     * 
     * @return The Plug-in Admin component's instance.
     */
    public PluginAdmin getPluginAdmin() {
    	return this.getService(PluginAdmin.class);
    }

    /**
     * Returns the locally stored DB component's instance.
     * 
     * @return The DB component's instance.
     */
    public DBService getDBService() {
        //return (DBServiceImpl)instances.get(DBService.class);
        return DBServiceImpl.getInstance(); // <-- Ugly but required for testing.
    }
    
    /**
     * Returns the locally stored FDS component's instance.
     * <br/>
     * 
     * @return The FDS component's instance.
     */
    public FDSService getFDSService() {
    	return this.getService(FDSService.class);
    }

    /**
     * Returns the locally stored Scheduler component's instance.
     * <br/>
     * 
     * @return The Scheduler component's instance.
     */
    public Scheduler getScheduler() {
    	return this.getService(Scheduler.class);
    }

    /**
     * Returns the locally stored Security component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The Security component's instance.
     */
    public SecurityManager getSecurityManager() {
    	if (this.securityManager == null) this.securityManager = new SecurityManager();
    	return this.securityManager;
    }

    /**
     * Returns the locally stored TDS component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The TDS component's instance.
     */
    public TDSService getTDSService() {
    	return this.getService(TDSService.class);
    }

    /**
     * Returns the locally stored Updater component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The Updater component's instance.
     */
    public UpdaterService getUpdater() {
    	return this.getService(UpdaterService.class);
    }

    /**
     * Returns the locally stored ClusterNodeService component's instance.
     * <br/>
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The ClusterNodeSerive component's instance.
     */
    public ClusterNodeService getClusterNodeService() {
    	return this.getService(ClusterNodeService.class);
    }

    /**
     * Returns the locally stored Metric Activator component's instance.
     * 
     * <i>The instance is created when this method is called for a first
     * time.</i>
     * 
     * @return The Metric Activator component's instance.
     */
    public MetricActivator getMetricActivator() {
    	return this.getService(MetricActivator.class);
    }
    
    /**
     * Returns the locally stored Administration Service component's instance.
     * 
     * @return The Administration Service component's instance.
     */
    public AdminService getAdminService() {
    	return this.getService(AdminService.class);
    }
	
	private void err(String msg) {
		System.err.println("AlitheiaCore: " + msg);
	}
}

// vi: ai nosi sw=4 ts=4 expandtab
