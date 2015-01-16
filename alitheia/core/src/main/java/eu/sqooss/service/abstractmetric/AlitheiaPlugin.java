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

import java.util.Date;
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

/**
 * This interface defines the common metric plug-in related functionality.
 * It must be implemented by all metric plug-ins.
 * <br/>
 * There are four areas of functionality covered by this interface:
 * <ul>
 *   <li> metric meta-data (describing the metric plug-in)
 *   <li> evaluation measurements
 *   <li> plug-in life cycle (installation and removal routines)
 *   <li> configuration management
 * </ul>
 * <br/><br/>
 * The metric meta-data comprises plug-in name, description, author
 * information and installation date; which are static for each metric
 * plug-in.
 *<br/><br/>
 * Measurement comprises two methods: <code>run()</code> which performs a
 * measurement on some project artifact (<i>which one depends on the type of
 * <code>DAObject</code> which is passed in</i>) and <code>getResult()</code>,
 * which returns the value(s) obtained by a previous measurement.
 * <br/><br/>
 * Life-cycle management is implemented in three methods:
 * <ul>
 *   <li> install
 *   <li> remove
 *   <li> update
 * </ul>
 * These takes care of the proper initialization and setup of a metric plug-in
 * during installation, as well as its proper removal when it is no more
 * needed.
 * <br/><br/>
 * Finally, configuration management deals with configuration settings, that
 * each plug-in can be accompanied with. A configuration property comprises of
 * a name, value, type, and a description tuple, and is stored directly into a
 * SQO-OSS database object that represents that configuration entry.
 *<br/><br/>
 * All metrics are bound to one or more of the following project resources:
 * <ul>
 *   <li>Project</li>
 *   <li>Project Version</li>
 *   <li>File Group</li>
 *   <li>File</li>
 *   <li>Mail Message</li>
 *   <li>Mailing List</li>
 *   <li>Bug</li>
 *   <li>Developer</li>
 *  </ul>
 *
 */
public interface AlitheiaPlugin extends MetricMetaData, MetricMeasurementEvaluation, MetricLifeCycle, MetricConfiguration{

}
