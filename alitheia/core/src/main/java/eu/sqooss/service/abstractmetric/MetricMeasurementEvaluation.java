package eu.sqooss.service.abstractmetric;

import java.util.List;

import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.Metric;
import eu.sqooss.service.scheduler.Job;

public interface MetricMeasurementEvaluation {
	/**
     * This method performs a measurement for
     * the given DAO, if possible. The DAO might be any one of the types
     * that make sense for measurements -- ProjectVersion, projectFile,
     * some others. If a DAO of a type that the metric doesn't support
     * is passed in, throws a MetricMismatchException.
     *
     * The calculation of measurements may be a computationally expensive
     * task, so metrics should start jobs (by themselves) to handle that.
     * The subclass AbstractMetric handles job creation automatically for
     * metrics that have simple requirements (a single job for doing the
     * calculation).
     *
     * Note that even if you use (parallel running) jobs in your jobs, the
     * metric's run method needs to block until the result is calculated.
     *
     * @param o The DAO that gets passed to the plug-in in order to run it
     * @throws MetricMismatchException if the DAO is of an unsupported type.
     * @throws AlreadyProcessingException to signify that the provided DAO is 
     * currently locked for processing by another thread. 
     * @throws Exception Any other exception initiated from the plugin code
     */
    void run(DAObject o) throws MetricMismatchException, 
        AlreadyProcessingException, Exception;
    
    /**
     * Get the metric result, without triggering a metric recalculation if
     * the result is not present.
     * If the result was not calculated yet, the result set is empty. If you
     * want to trigger the calculation to get a result, use getResult() instead.
     *
     * @param o DAO whose type specifies the specialized sub-interface to use
     *          and whose value determines which result to get.
     * @return l A list of metrics
     * @return value of the measurement or null if there is no such measurement.
     * @throws MetricMismatchException if the DAO type is one not supported by
     *          this metric.
     */
    List<Result> getResultIfAlreadyCalculated(DAObject o, List<Metric> l)
    	throws MetricMismatchException;

    /**
     * Get a metric result. 
     * If the result was not calculated yet, the plugin's run method is called,
     * and the request waits until the run method returns.
     * If you don't want this behavior, use getResultIfAlreadyCalculated()
     * instead.
     *
     * @param o DAO whose type specifies the specialized sub-interface to use
     *          and whose value determines which result to get.
     * @return l A list of metrics
     * @return value of the measurement or null if there is no such measurement.
     * @throws MetricMismatchException if the DAO type is one not supported by
     *          this metric.
     * @throws AlreadyProcessingException to signify that the provided DAO is 
     * currently locked for processing by another thread.  
     * @throws Exception All exceptions initiated by the errors in code 
     * included in implemenations of those classes.           
     */
    List<Result> getResult(DAObject o, List<Metric> l)
        throws MetricMismatchException, AlreadyProcessingException, Exception;
    
    /**
     * Set a reference to the scheduler job that executes the metric. The
     * job reference is only set when the metric's execute method is called.
     */
    void setJob(Job j);
}
