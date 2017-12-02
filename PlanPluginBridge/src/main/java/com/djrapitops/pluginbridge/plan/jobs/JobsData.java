/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.jobs;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.dao.JobsDAOData;
import main.java.com.djrapitops.plan.data.element.AnalysisContainer;
import main.java.com.djrapitops.plan.data.element.InspectContainer;
import main.java.com.djrapitops.plan.data.element.TableContainer;
import main.java.com.djrapitops.plan.data.plugin.ContainerSize;
import main.java.com.djrapitops.plan.data.plugin.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Jobs plugin.
 *
 * @author Rsl1122
 */
public class JobsData extends PluginData {

    public JobsData() {
        super(ContainerSize.THIRD, "Jobs");
        super.setIconColor("brown");
        super.setPluginIcon("suitcase");
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        List<JobsDAOData> playersJobs = Jobs.getDBManager().getDB().getAllJobs(null, uuid);

        TableContainer jobTable = new TableContainer(getWithIcon("Job", "suitcase"), getWithIcon("Level", "plus"));
        for (JobsDAOData job : playersJobs) {
            jobTable.addRow(job.getJobName(), job.getLevel());
        }
        if (playersJobs.isEmpty()) {
            jobTable.addRow("No Jobs");
        }
        inspectContainer.addTable("jobTable", jobTable);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        List<JobsDAOData> allJobs = Jobs.getDBManager().getDB().getAllJobs()
                .values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        TableContainer jobTable = new TableContainer(getWithIcon("Job", "suitcase"), getWithIcon("Workers", "users"), getWithIcon("Total Level", "plus"), getWithIcon("Average Level", "plus"));

        if (allJobs.isEmpty()) {
            jobTable.addRow("No Jobs with Workers");
        } else {
            Map<String, Integer> workers = new HashMap<>();
            Map<String, Long> totals = new HashMap<>();
            for (JobsDAOData data : allJobs) {
                String job = data.getJobName();
                int level = data.getLevel();
                workers.put(job, workers.getOrDefault(job, 0) + 1);
                totals.put(job, totals.getOrDefault(job, 0L) + level);
            }

            List<String> order = new ArrayList<>(workers.keySet());
            Collections.sort(order);

            for (String job : order) {
                int amountOfWorkers = workers.getOrDefault(job, 0);
                long totalLevel = totals.getOrDefault(job, 0L);
                jobTable.addRow(
                        job,
                        amountOfWorkers,
                        FormatUtils.cutDecimals(MathUtils.averageDouble(totalLevel, amountOfWorkers)),
                        totalLevel
                );
            }
        }
        analysisContainer.addTable("jobTable", jobTable);


        return analysisContainer;
    }
}