package eu.sqooss.service.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.sqooss.service.db.ProjectVersion;
import eu.sqooss.service.db.StoredProject;

public class ProjectVersionDateUtils {
	public List<ProjectVersion> getPreviousMonthVersions(ProjectVersion pv) {
        List<ProjectVersion> versions = new ArrayList<ProjectVersion>();
        versions.add(pv);
        ProjectVersion prev = pv.getPreviousVersion();
        long monthsecs = 3600 * 24 * 30;
        while (true) {
            //Diff in seconds
            long diff = (pv.getTimestamp() - prev.getTimestamp()) / 1000;
            if (prev != null && diff <  monthsecs ) {
                versions.add(prev);
            } else {
                break;
            }
            prev = prev.getPreviousVersion();
        }
        return versions;
    }
    
    public List<ProjectVersion> getNextWeekVersions(ProjectVersion pv) {
        List<ProjectVersion> versions = new ArrayList<ProjectVersion>();
        ProjectVersion next = pv.getNextVersion();
        long weeksecs = 3600 * 24 * 7;
        while (true) {
            long diff = (next.getTimestamp() - pv.getTimestamp()) / 1000;
            if (next != null && diff < weeksecs) {
                versions.add(next);
            } else {
                break;
            }
            next = next.getNextVersion();
        }
        
        return versions;
    }
}
