package eu.sqooss.service.abstractmetric;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType;
import eu.sqooss.service.logging.Logger;

public class MetricDiscovery implements Iterable<Metric>{
	Logger log = AlitheiaCore.getInstance().getLogManager().createLogger(Logger.NAME_SQOOSS_METRIC);
	
	private AlitheiaPlugin plugin;
	
	/** 
     * Metric mnemonics for the metrics required to be present for this 
     * metric to operate.
     */
    private Set<String> dependencies = new HashSet<String>();
    private Map<String, Metric> metrics;
    
    /** The list of this plug-in's activators*/
    private Set<Class<? extends DAObject>> activators =
        new HashSet<Class<? extends DAObject>>();

    private Map<Metric, List<Class<? extends DAObject>>> metricActType =
    	new HashMap<Metric, List<Class<? extends DAObject>>>();

    
	public MetricDiscovery(AlitheiaPlugin plugin) {
		this.plugin = plugin;
		
		discoverMetrics();
	}
	
	private void discoverMetrics()
	{
        /*Discover the declared metrics*/
        MetricDeclarations md = this.getClass().getAnnotation(MetricDeclarations.class);

		if (md != null && md.metrics().length > 0) {
			for (MetricDecl metric : md.metrics()) {
				log.debug("Found metric: " + metric.mnemonic() + " with "
						+ metric.activators().length + " activators");

				if (metrics.containsKey(metric.mnemonic())) {
				    log.error("Duplicate metric mnemonic " + metric.mnemonic());
				    continue;
				}
				
				Metric m = new Metric();
				m.setDescription(metric.descr());
				m.setMnemonic(metric.mnemonic());
				m.setMetricType(new MetricType(MetricType.fromActivator(metric.activators()[0])));
			
				List<Class<? extends DAObject>> activs = new ArrayList<Class<? extends DAObject>>();				
				for (Class<? extends DAObject> o : metric.activators()) {
					activs.add(o);
				}
				
				metricActType.put(m, activs);
				
				activators.addAll(Arrays.asList(metric.activators()));
				
				metrics.put(m.getMnemonic(), m);
				if (metric.dependencies().length > 0)
					dependencies.addAll(Arrays.asList(metric.dependencies()));
			}
		} else {
			log.warn("Plug-in " + plugin.getName() + " declares no metrics");
		}
	}

	/**
     * Check if the plug-in dependencies are satisfied
     */
    public boolean checkDependencies() {
        for (String mnemonic : dependencies) {
        	//Check thyself first
        	if (metrics.containsKey(mnemonic))
        		continue;
        	
            if (AlitheiaCore.getInstance().getPluginAdmin().getImplementingPlugin(mnemonic) == null) {
                log.error("No plug-in implements metric "  + mnemonic + 
                        " which is required by " + plugin.getName());
                return false;
            }
        }
        return true;
    }
    
    @Override
    public Iterator<Metric> iterator() {
    	return metrics.values().iterator();
    }
    
    public Set<Class<? extends DAObject>> getActivators() {
		return activators;
	}
    
    public Set<String> getDependencies() {
		return dependencies;
	}
    
    public Map<Metric, List<Class<? extends DAObject>>> getMetricActType() {
		return metricActType;
	}
}
