/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2008 - 2010 - Organization for Free and Open Source Software,  
 *                 Athens, Greece.
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
package eu.sqooss.service.abstractmetric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.osgi.framework.BundleContext;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.EncapsulationUnitMeasurement;
import eu.sqooss.service.db.ExecutionUnitMeasurement;
import eu.sqooss.service.db.MailMessageMeasurement;
import eu.sqooss.service.db.MailingListThreadMeasurement;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.db.MetricMeasurement;
import eu.sqooss.service.db.MetricType.Type;
import eu.sqooss.service.db.NameSpaceMeasurement;
import eu.sqooss.service.db.PluginConfiguration;
import eu.sqooss.service.db.ProjectFileMeasurement;
import eu.sqooss.service.db.ProjectFileState;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.ProjectVersionMeasurement;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.db.StoredProjectMeasurement;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.metricactivator.MetricActivationException;
import eu.sqooss.service.pa.PluginAdmin;
import eu.sqooss.service.pa.PluginInfo;
import eu.sqooss.service.scheduler.Job;

/**
 * A base class for all metrics. Implements basic functionality such as
 * logging setup and plug-in information retrieval from the OSGi bundle
 * manifest file. Metrics can choose to directly implement
 * the {@link eu.sqooss.abstractmetric.AlitheiaPlugin} interface instead of 
 * extending this class.
 */
public class DefaultMetric implements AlitheiaPlugin {

    /** Reference to the metric bundle context */
    protected BundleContext bc;

    /** Logger for administrative operations */
    protected Logger log = null;

    /** Reference to the DB service, not to be passed to metric jobs */
    protected DBService db;

    /** 
     * Reference to the plugin administrator service, not to be passed to 
     * metric jobs 
     */
    protected PluginAdmin pa;
    
    protected MetricDiscovery metricDiscovery;
    protected MetricMetaData metricMetadata;
    protected MetricMeasurementEvaluation metricEvaluation;
    protected MetricLifeCycle metricLifeCycle;
    protected MetricConfiguration metricConfiguration;
    

    /**
     * Init basic services common to all implementing classes
     * @param bc - The bundle context of the implementing metric - to be passed
     * by the activator.
     */
    protected DefaultMetric(BundleContext bc) {

        this.bc = bc;
       
        log = AlitheiaCore.getInstance().getLogManager().createLogger(Logger.NAME_SQOOSS_METRIC);

        if (log == null) {
            System.out.println("ERROR: Got no logger");
        }

        db = AlitheiaCore.getInstance().getDBService();

        if(db == null)
            log.error("Could not get a reference to the DB service");

        pa = AlitheiaCore.getInstance().getPluginAdmin();

        if(pa == null)
            log.error("Could not get a reference to the Plugin Administation "
                    + "service");
        
        this.metricDiscovery = new MetricDiscovery(this);
        this.metricMetadata = new DefaultMetricMetaData(this, bc);
        this.metricEvaluation = new DefaultMeasurementEvaluation(this, metricDiscovery);
        this.metricLifeCycle = new DefaultMetricLifeCycle(bc, this, metricDiscovery);
        this.metricConfiguration = new DefaultMetricConfiguration(bc, this, metricDiscovery);
     }
    
    
    protected List<ProjectFileMeasurement> QRY_SOURCE_DIRS(ProjectVersion pv, String MNOL, String ISSRCDIR){
    	String paramIsDirectory = "is_directory";
        String paramMNOL = "paramMNOL";
        String paramISSRCDIR = "paramISSRCDIR";
        String paramVersionId = "paramVersionId";
        String paramProjectId = "paramProjectId";
        String paramState = "paramStatus";
        
        
        Map<String,Object> params = new HashMap<String,Object>();

        StringBuffer q = new StringBuffer("select pfm ");
        if (pv.getSequence() == ProjectVersion.getLastProjectVersion(pv.getProject()).getSequence()) {
            q.append(" from ProjectFile pf, ProjectFileMeasurement pfm");
            q.append(" where pf.validUntil is null ");
        } else {
            q.append(" from ProjectVersion pv, ProjectVersion pv2,");
            q.append(" ProjectVersion pv3, ProjectFile pf, ");
            q.append(" ProjectFileMeasurement pfm ");
            q.append(" where pv.project.id = :").append(paramProjectId);
            q.append(" and pv.id = :").append(paramVersionId);
            q.append(" and pv2.project.id = :").append(paramProjectId);
            q.append(" and pv3.project.id = :").append(paramProjectId);
            q.append(" and pf.validFrom.id = pv2.id");
            q.append(" and pf.validUntil.id = pv3.id");
            q.append(" and pv2.sequence <= pv.sequence");
            q.append(" and pv3.sequence >= pv.sequence");
            
            params.put(paramProjectId, pv.getProject().getId());
            params.put(paramVersionId, pv.getId());
        }

        q.append(" and pf.state <> :").append(paramState);
        q.append(" and pf.isDirectory = :").append(paramIsDirectory);
        q.append(" and pfm.projectFile = pf");
        q.append(" and pfm.metric = :").append(paramMNOL);
        q.append(" and exists (select pfm1 ");
        q.append(" from ProjectFileMeasurement pfm1 ");
        q.append(" where pfm1.projectFile = pfm.projectFile ");
        q.append(" and pfm1.metric = :").append(paramISSRCDIR).append(")");
        
        params.put(paramState, ProjectFileState.deleted());
        params.put(paramIsDirectory, true);
        params.put(paramMNOL, Metric.getMetricByMnemonic(MNOL));
        params.put(paramISSRCDIR, Metric.getMetricByMnemonic(ISSRCDIR));
        return (List<ProjectFileMeasurement>) db.doHQL(q.toString(), params);
    }
    
    
        /**
     * Add an entry to this plug-in's configuration schema.
     *
     * @param name The name of the configuration property
     * @param defValue The default value for the configuration property
     * @param msg The description of the configuration property
     * @param type The type of the configuration property
     */
    protected final void addConfigEntry(String name, String defValue,
            String msg, PluginInfo.ConfigurationType type) {
        // Retrieve the plug-in's info object
        PluginInfo pi = pa.getPluginInfo(getUniqueKey());
        // Will happen if called during bundle's startup
        if (pi == null) {
            log.warn("Adding configuration key <" + name +
                "> to plugin <" + getName() + "> failed: " +
                "no PluginInfo.");
            return;
        }
        // Modify the plug-in's configuration
        try {
            // Update property
            if (pi.hasConfProp(name, type.toString())) {
                if (pi.updateConfigEntry(db, name, defValue)) {
                    // Update the Plug-in Admin's information
                    pa.pluginUpdated(pa.getPlugin(pi));
                }
                else {
                    log.error("Property (" + name +") update has failed!");
                }
            }
            // Create property
            else {
                if (pi.addConfigEntry(
                        db, name, msg, type.toString(), defValue)) {
                    // Update the Plug-in Admin's information
                    pa.pluginUpdated(pa.getPlugin(pi));
                }
                else {
                    log.error("Property (" + name +") append has failed!");
                }
            }
        }
        catch (Exception ex){
            log.error("Can not modify property (" + name +") for plugin ("
                    + getName(), ex);
        }
    }

    /**
     * Remove an entry from the plug-in's configuration schema
     *
     * @param name The name of the configuration property to remove
     * @param name The type of the configuration property to remove
     */
    protected final void removeConfigEntry(
            String name,
            PluginInfo.ConfigurationType type) {
        // Retrieve the plug-in's info object
        PluginInfo pi = pa.getPluginInfo(getUniqueKey());
        // Will happen if called during bundle's startup
        if (pi == null) {
            log.warn("Removing configuration key <" + name +
                "> from plugin <" + getName() + "> failed: " +
                "no PluginInfo.");
            return;
        }
        // Modify the plug-in's configuration
        try {
            if (pi.hasConfProp(name, type.toString())) {
                if (pi.removeConfigEntry(db, name, type.toString())) {
                    // Update the Plug-in Admin's information
                    pa.pluginUpdated(pa.getPlugin(pi));
                }
                else {
                    log.error("Property (" + name +") remove has failed!");
                }
            }
            else {
                log.error("Property (" + name +") does not exist!");
            }
        }
        catch (Exception ex){
            log.error("Can not remove property (" + name +") from plugin ("
                    + getName() + ")", ex);
        }
    }
    
    /**
     * Retrieve author information from the plug-in bundle
     */
    public String getAuthor() {
    	return metricMetadata.getAuthor();
    }

    /**
     * Retrieve the plug-in description from the plug-in bundle
     */
    public String getDescription() {
    	return metricMetadata.getDescription();
    }

    /**
     * Retrieve the plug-in name as specified in the metric bundle
     */
    public String getName() {
    	return metricMetadata.getName();
    }

    /**
     * Retrieve the plug-in version as specified in the metric bundle
     */
    public String getVersion() {
    	return metricMetadata.getVersion();
    }
    
    @Override
	public Date getDateInstalled() {
		return metricMetadata.getDateInstalled();
	}
    
    @Override
    public List<Result> getResult(DAObject o, List<Metric> l)
    		throws MetricMismatchException, AlreadyProcessingException,
    		Exception {
    	return metricEvaluation.getResult(o, l);
    }
    
    private static Map<Class<? extends MetricMeasurement>, String> resultFieldNames = 
            new HashMap<Class<? extends MetricMeasurement>, String>();
        
        static {
            resultFieldNames.put(StoredProjectMeasurement.class, "storedProject");
            resultFieldNames.put(ProjectVersionMeasurement.class, "projectVersion");
            resultFieldNames.put(ProjectFileMeasurement.class, "projectFile");
            resultFieldNames.put(MailMessageMeasurement.class, "mail");
            resultFieldNames.put(MailingListThreadMeasurement.class, "thread");
            resultFieldNames.put(ExecutionUnitMeasurement.class, "executionUnit");
            resultFieldNames.put(EncapsulationUnitMeasurement.class, "encapsulationUnit");
            resultFieldNames.put(NameSpaceMeasurement.class, "namespace");
        }
    
    /**
     * Convenience method to get the measurement for a single metric.
     */
    protected List<Result> getResult(DAObject o, Class<? extends MetricMeasurement> clazz, 
            Metric m, Result.ResultType type) {
        DBService dbs = AlitheiaCore.getInstance().getDBService();
        Map<String, Object> props = new HashMap<String, Object>();
        
        props.put(resultFieldNames.get(clazz), o);
        props.put("metric", m);
        List resultat = dbs.findObjectsByProperties(clazz, props);
        
        if (resultat.isEmpty())
            return Collections.EMPTY_LIST;
        
        ArrayList<Result> result = new ArrayList<Result>();
        result.add(new Result(o, m, ((MetricMeasurement)resultat.get(0)).getResult(), type));
        return result;
        
    }
    
    @Override
    public List<Result> getResultIfAlreadyCalculated(DAObject o, List<Metric> l)
    		throws MetricMismatchException {
    	return metricEvaluation.getResultIfAlreadyCalculated(o, l);
    }
    
    @Override
    public void run(DAObject o) throws MetricMismatchException,
    		AlreadyProcessingException, Exception {
    	metricEvaluation.run(o);
    }
    
    @Override
    public void setJob(Job j) {
    	metricEvaluation.setJob(j);
    }
    
    @Override
    public boolean update() {
    	return metricLifeCycle.update();
    }
    
    @Override
    public boolean install() {
    	return metricLifeCycle.install();
    }
    
    @Override
    public boolean remove() {
    	return metricLifeCycle.remove();
    }
    
    @Override
    public boolean cleanup(DAObject sp) {
    	return metricConfiguration.cleanup(sp);
    }
    
    @Override
    public String getUniqueKey() {
    	return metricConfiguration.getUniqueKey();
    }
    
    @Override
    public Set<Class<? extends DAObject>> getActivationTypes() {
    	return metricConfiguration.getActivationTypes();
    }
    
    @Override
    public Set<PluginConfiguration> getConfigurationSchema() {
    	return metricConfiguration.getConfigurationSchema();
    }
    
    @Override
    public Set<String> getDependencies() {
    	return metricConfiguration.getDependencies();
    }
    
    @Override
    public Map<Type, SortedSet<Long>> getObjectIdsToSync(StoredProject sp,
    		Metric m) throws MetricActivationException {
    	return metricConfiguration.getObjectIdsToSync(sp, m);
    }
    
    @Override
    public List<Metric> getAllSupportedMetrics() {
    	return metricConfiguration.getAllSupportedMetrics();
    }
    
    @Override
    public List<Metric> getSupportedMetrics(Class<? extends DAObject> activator) {
    	return metricConfiguration.getSupportedMetrics(activator);
    }
    
    @Override
    public PluginConfiguration getConfigurationOption(String config) {
    	return metricConfiguration.getConfigurationOption(config);
    }

	@Override
	public List<Class<? extends DAObject>> getMetricActivationTypes(Metric m) {
		return metricConfiguration.getMetricActivationTypes(m);
	}
 }
