package eu.sqooss.service.abstractmetric;

public interface MetricLifeCycle {
	/**
     * After installing a new version of the metric, try to
     * update the results. The metric may opt to partially
     * or fully update its results tables or files.
     *
     * @return True, if the update succeeded, false otherwise
     */
    boolean update();

    /**
     * Perform maintenance operations when installing a new
     * version of the metric
     *
     * @return True if installation succeeded, false otherwise
     */
    boolean install();

    /**
     * Free the used resources and clean up on metric removal
     *
     * @return True, if the removal succeeded, false otherwise
     */
    boolean remove();
}
