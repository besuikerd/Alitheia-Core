/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 *
 * Copyright 2007-2008 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007-2008 by the Georgios Gousios <gousiosg@gmail.com>
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
package eu.sqooss.service.metricactivator;

import java.util.SortedSet;

import eu.sqooss.service.abstractmetric.AlitheiaPlugin;
import eu.sqooss.service.db.DAObject;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;

/**
 * The MetricActivator service is responsible for kickstarting metric jobs 
 * either after project metadata updates or  
 */
public interface MetricActivator {

    /**
     * Run the metrics that  
     * 
     * @param clazz 
     * @param objectIDs 
     */
    public <T extends DAObject> void runMetrics(Class<T> clazz, SortedSet<Long> objectIDs);
    
    /**
     * Synchronize metric results for all metrics that get activated by the
     * event types identified by clazz with the latest project state
     * 
     * @param clazz
     * @param sp
     */
    public <T extends DAObject> void syncMetrics(Class<T> clazz, StoredProject sp);
    
    /**
     * 
     * 
     * @param m
     * @param sp
     */
    public void syncMetric(AlitheiaPlugin m, StoredProject sp);
    
    /**
     * 
     * @param m
     * @param sp
     * @return
     */
    public ProjectVersion getLastAppliedVersion(AlitheiaPlugin m, StoredProject sp);
    
}