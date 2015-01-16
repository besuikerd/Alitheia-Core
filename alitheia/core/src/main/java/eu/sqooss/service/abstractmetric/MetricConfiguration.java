package eu.sqooss.service.abstractmetric;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricType;
import eu.sqooss.service.db.PluginConfiguration;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.metricactivator.MetricActivationException;
import eu.sqooss.service.scheduler.Job;

public interface MetricConfiguration {
	/**
     * Clean results on project removal
     * 
     * @param sp The DAO to be used as reference when cleaning up results.
     * @return True, if the cleanup succeeded, false otherwise
     */
    boolean cleanup(DAObject sp);

    /**
     * Return a string that is unique for this plugin, used for indexing this
     * plugin to the system database
     *
     * @return A unique string, max length 255 characters
     */
    String getUniqueKey();

    /**
     * Get the types supported by this plug-in for data processing and result
     * retrieval. An activation type is DAO subclass which is passed as argument
     * to the {@link AlitheiaPlugin.run()} and
     * {@link AlitheiaPlugin.getResult()}} methods to trigger metric
     * calculation and result retrieval.
     *
     * @return A set of DAObject subclasses
     */
    Set<Class<? extends DAObject>> getActivationTypes();

    /**
     * Get the activation type that corresponds to the activation type which 
     * the metric result is stored. 
     * 
     * @param m - The metric for which to search for an activation type
     * @return A list of subclasses of DAObject (a.k.a activation types).
     */
    List<Class<? extends DAObject>> getMetricActivationTypes (Metric m);

    /**
     * Retrieves the list of configuration properties for this plug-in.
     * <br/>
     * Metric plug-ins can use the <code>AbstractMetric</code>'s
     * <code>addConfigEntry</code> and <code>removeConfigEntry</code> methods
     * to manage their own configuration schema.
     *
     * @return The set of the existing configuration properties for
     *   this plug-in. This may be an empty list if no configuration is
     *   needed or if the plug-in is not active.
     */
    Set<PluginConfiguration> getConfigurationSchema();

    /**
     * Metric mnemonics for the metrics required to be present for this 
     * plugin to operate. 
     * 
     * @return A, possibly empty, set of metric mnemonics. 
     */
    Set<String> getDependencies();

    /**
     * Get a list of object ids for the database entities to run the metric
     * on, ordered by activation type. This method essentially allows the plugin
     * to specify a custom processing order for metadata entities to be processed
     * by metrics. The default execution order is specified 
     * 
     */
    Map<MetricType.Type, SortedSet<Long>> getObjectIdsToSync(StoredProject sp, Metric m) 
    	throws MetricActivationException;
    
    public List<Metric> getAllSupportedMetrics();
    
    public List<Metric> getSupportedMetrics(Class<? extends DAObject> activator);
    
    /**
	 * Get a configuration option for this metric from the plugin configuration
	 * store
	 * 
	 * @param config
	 *            The configuration option to retrieve
	 * @return The configuration entry corresponding the provided description or
	 *         null if not found in the plug-in's configuration schema
	 */
	public PluginConfiguration getConfigurationOption(String config);
}
