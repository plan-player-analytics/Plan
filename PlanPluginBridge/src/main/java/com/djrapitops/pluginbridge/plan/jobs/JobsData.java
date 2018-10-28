/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.pluginbridge.plan.jobs;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.dao.JobsDAOData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for Jobs plugin.
 *
 * @author Rsl1122
 */
class JobsData extends PluginData {

    private final Formatter<Double> decimalFormatter;

    JobsData(Formatter<Double> decimalFormatter) {
        super(ContainerSize.THIRD, "Jobs");
        this.decimalFormatter = decimalFormatter;
        setPluginIcon(Icon.called("suitcase").of(Color.BROWN).build());
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        List<JobsDAOData> playersJobs = Jobs.getDBManager().getDB().getAllJobs(null, uuid);

        TableContainer jobTable = new TableContainer(
                getWithIcon("Job", Icon.called("suitcase")),
                getWithIcon("Level", Icon.called("plus")));
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
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        List<JobsDAOData> allJobs = Jobs.getDBManager().getDB().getAllJobs()
                .values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        TableContainer jobTable = new TableContainer(
                getWithIcon("Job", Icon.called("suitcase")),
                getWithIcon("Workers", Icon.called("users")),
                getWithIcon("Total Level", Icon.called("plus")),
                getWithIcon("Average Level", Icon.called("plus"))
        );

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
                        totalLevel,
                        amountOfWorkers != 0 ? decimalFormatter.apply(totalLevel * 1.0 / amountOfWorkers) : "-"
                );
            }
        }
        analysisContainer.addTable("jobTable", jobTable);

        return analysisContainer;
    }
}