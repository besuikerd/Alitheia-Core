/*
 * This file is part of the Alitheia system, developed by the SQO-OSS
 * consortium as part of the IST FP6 SQO-OSS project, number 033331.
 * 
 * Copyright 2007 by the SQO-OSS consortium members <info@sqo-oss.eu>
 * Copyright 2007 Georgios Gousios <gousiosg@aueb.gr>
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

package eu.sqooss.impl.service.updater;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import eu.sqooss.service.logging.LogManager;
import eu.sqooss.service.logging.Logger;
import eu.sqooss.service.updater.UpdaterService;
import eu.sqooss.service.tds.CommitEntry;
import eu.sqooss.service.tds.CommitLog;
import eu.sqooss.service.tds.Diff;
import eu.sqooss.service.tds.ProjectRevision;
import eu.sqooss.service.tds.SCMAccessor;
import eu.sqooss.service.tds.TDSService;
import eu.sqooss.service.db.DBService;
import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;
import eu.sqooss.service.scheduler.Scheduler;

public class SourceUpdater {

    private String path;

    private TDSService tds;

    private DBService dbs;

    private Scheduler scheduler;

    private Logger logger;

    public SourceUpdater(String path, TDSService tds, DBService dbs,
            Scheduler scheduler, Logger logger) throws UpdaterException {
        if ((path == null) || (tds == null) || (dbs == null)
                || (scheduler == null) || (logger == null)) {
            throw new UpdaterException(
                    "The components required by the updater are unavailable.");
        }

        this.tds = tds;
        this.dbs = dbs;
        this.scheduler = scheduler;
        this.logger = logger;
    }

    public void DoUpdate() throws UpdaterException {

        try {

            StoredProject project = getProject();

            ProjectVersion lastVersion = getLastProjectVersion(project);

            SCMAccessor scm = tds.getAccessor(project.getId()).getSCMAccessor();

            CommitLog commitLog = scm.getCommitLog(new ProjectRevision(
                    lastVersion.getVersion()), new ProjectRevision(new Date()));

            for (CommitEntry entry : commitLog) {

            }

            Diff diff = getProjectDiff(lastVersion, scm);

            Set<String> changedFiles = diff.getChangedFiles();
        } catch (Exception e) {
            e.getMessage();
        }

    }

    private Diff getProjectDiff(ProjectVersion lastVersion, SCMAccessor scm)
            throws UpdaterException {
        Diff diff = null;
        try {
            diff = scm.getDiff("/", new ProjectRevision(lastVersion
                    .getVersion()), new ProjectRevision(new Date()));
        } catch (Exception ex) {
            throw new UpdaterException(
                    "Updater failed to retrieve the diff for the project "
                            + path + ":\n" + ex.getMessage());
        }
        return diff;
    }

    private StoredProject getProject() throws UpdaterException {
        StoredProject project = null;

        List prList = dbs.doHQL("from StoredProject where Name = " + path + "");
        if ((prList == null) || (prList.size() != 1)) {
            throw new UpdaterException("The requested project was not found");
        }

        project = (StoredProject) prList.get(0);
        return project;
    }

    private ProjectVersion getLastProjectVersion(StoredProject project)
            throws UpdaterException {
        ProjectVersion lastVersion = null;

        List pvList = dbs.doHQL("from ProjectVersion pv where pv.project = "
                + project.getId() + " and pv.id = (select max(pv2.id) from "
                + " ProjectVersion pv2 where pv2.project = " + project.getId());

        if ((pvList == null) || (pvList.size() != -1)) {
            throw new UpdaterException(
                    "The last stored version of the project could not be retrieved");
        }

        lastVersion = (ProjectVersion) pvList.get(0);
        return lastVersion;
    }

}

//vi: ai nosi sw=4 ts=4 expandtab