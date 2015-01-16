package eu.sqooss.service.abstractmetric;

import java.util.Date;

public interface MetricMetaData {
	/**
     * Get the metric version. Free form text.
     *
     * @return The metric's version.
     */
    String getVersion();

    /**
     * Get information about the metric author
     *
     * @return The metric's author.
     */
    String getAuthor();

    /**
     * Get the date this version of the metric has been installed
     *
     * @return The metric's installation date.
     */
    Date getDateInstalled();

    /**
     * Get the metric name
     *
     * @return The metric's name.
     */
    String getName();

    /**
     * Get a free text description of what this metric calculates
     *
     * @return The metric's description.
     */
    String getDescription();
}
