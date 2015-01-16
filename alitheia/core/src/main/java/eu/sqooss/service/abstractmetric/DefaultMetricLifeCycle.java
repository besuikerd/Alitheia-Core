package eu.sqooss.service.abstractmetric;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.db.MetricType.Type;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivator;

public class DefaultMetricLifeCycle implements MetricLifeCycle{
	Logger log = AlitheiaCore.getInstance().getLogManager().createLogger(Logger.NAME_SQOOSS_METRIC);
	
	private AlitheiaPlugin plugin;
	private MetricDiscovery metricDiscovery;
	
	private DBService db;
	private BundleContext bc;
	
	public DefaultMetricLifeCycle(BundleContext bc, AlitheiaPlugin plugin, MetricDiscovery metricDiscovery){
		this.bc = bc;
		this.plugin = plugin;
		this.metricDiscovery = metricDiscovery;
		this.db = AlitheiaCore.getInstance().getDBService();
	}
	
	/**
     * Register the metric to the DB. Subclasses can run their custom
     * initialization routines (i.e. registering DAOs or tables) after calling
     * super().install()
     */
    public boolean install() {
        //1. check if dependencies are satisfied
        if (!metricDiscovery.checkDependencies()) {
            log.error("Plug-in installation failed");
            return false;
        }
        
        HashMap<String, Object> h = new HashMap<String, Object>();
        h.put("name", plugin.getName());

        List<Plugin> plugins = db.findObjectsByProperties(Plugin.class, h);

        if (!plugins.isEmpty()) {
            log.warn("A plugin with name <" + plugin.getName()
                    + "> is already installed, won't re-install.");
            return false;
        }


        //2. Add the plug-in
        Plugin p = new Plugin();
        p.setName(plugin.getName());
        p.setInstalldate(new Date(System.currentTimeMillis()));
        p.setVersion(plugin.getVersion());
        p.setActive(true);
        p.setHashcode(plugin.getUniqueKey());
        boolean result =  db.addRecord(p);
        
        //3. Add the metrics
        for (Metric m : metricDiscovery) {
        	Type type = Type.fromString(m.getMetricType().getType());
        	MetricType newType = MetricType.getMetricType(type);
        	if (newType == null) {
                newType = new MetricType(type);
                db.addRecord(newType);
                m.setMetricType(newType);
            }
        	
        	m.setMetricType(newType);
        	m.setPlugin(p);
        	db.addRecord(m);
        }
        
        return result;
    }

    /**
     * Remove a plug-in's record from the DB. The DB's referential integrity
     * mechanisms are expected to automatically remove associated records.
     * Subclasses should also clean up any custom tables created.
     */
    public boolean remove() {
        Plugin p = Plugin.getPluginByHashcode(plugin.getUniqueKey());
        return db.deleteRecord(p);
    }
    
    /**{@inheritDoc}}*/
    public boolean update() {
        ServiceReference serviceRef = null;
        serviceRef = bc.getServiceReference(AlitheiaCore.class.getName());

        MetricActivator ma =
            ((AlitheiaCore)bc.getService(serviceRef)).getMetricActivator();

        if (ma == null) {
            return false;
        }

        ma.syncMetrics(plugin);

        return true;
    }
}
