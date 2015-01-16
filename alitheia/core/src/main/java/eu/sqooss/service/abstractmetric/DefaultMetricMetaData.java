package eu.sqooss.service.abstractmetric;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.osgi.framework.BundleContext;

import eu.sqooss.core.AlitheiaCore;
import eu.sqooss.service.db.Plugin;
import eu.sqooss.service.logging.Logger;

public class DefaultMetricMetaData implements MetricMetaData{
	Logger log = AlitheiaCore.getInstance().getLogManager().createLogger(Logger.NAME_SQOOSS_METRIC);
	
	private AlitheiaPlugin plugin;
	
	private BundleContext bc;
	
	public DefaultMetricMetaData(AlitheiaPlugin plugin, BundleContext bc) {
		this.plugin = plugin;
		this.bc = bc;
	}

	/**
     * Retrieve author information from the plug-in bundle
     */
    public String getAuthor() {

        return (String) bc.getBundle().getHeaders().get(
                org.osgi.framework.Constants.BUNDLE_CONTACTADDRESS);
    }

    /**
     * Retrieve the plug-in description from the plug-in bundle
     */
    public String getDescription() {

        return (String) bc.getBundle().getHeaders().get(
                org.osgi.framework.Constants.BUNDLE_DESCRIPTION);
    }

    /**
     * Retrieve the plug-in name as specified in the metric bundle
     */
    public String getName() {

        return (String) bc.getBundle().getHeaders().get(
                org.osgi.framework.Constants.BUNDLE_NAME);
    }

    /**
     * Retrieve the plug-in version as specified in the metric bundle
     */
    public String getVersion() {

        return (String) bc.getBundle().getHeaders().get(
                org.osgi.framework.Constants.BUNDLE_VERSION);
    }
    
    /**
     * Retrieve the installation date for this plug-in version
     */
    public final Date getDateInstalled() {
        return Plugin.getPluginByHashcode(plugin.getUniqueKey()).getInstalldate();
    }
}