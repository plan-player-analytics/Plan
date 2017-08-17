package com.djrapitops.pluginbridge.plan.jobs;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.dao.JobsDAOData;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData class for Jobs-plugin.
 * <p>
 * Registered to the plugin by JobsHook
 *
 * @author Rsl1122
 * @see JobsHook
 * @since 3.2.1
 */
public class JobsAnalysisJobTable extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public JobsAnalysisJobTable() {
        super("Jobs", "analysis_table", AnalysisType.HTML);
        final String job = Html.FONT_AWESOME_ICON.parse("suitcase") + " Job";
        final String workers = Html.FONT_AWESOME_ICON.parse("users") + " Workers";
        final String tLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Total Level";
        final String aLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Avg Level";
        super.setPrefix(Html.TABLE_START_4.parse(job, workers, aLevel, tLevel));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        List<JobsDAOData> allJobs = Jobs.getDBManager().getDB().getAllJobs()
                .values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        if (allJobs.isEmpty()) {
            return parseContainer("", Html.TABLELINE_4.parse("No Players with Jobs", "", "", ""));
        }

        Map<String, Integer> workers = new HashMap<>();
        Map<String, Long> totals = new HashMap<>();
        for (JobsDAOData data : allJobs) {
            String job = data.getJobName();
            int level = data.getLevel();
            if (!workers.containsKey(job)) {
                workers.put(job, 0);
            }
            workers.put(job, workers.get(job) + 1);
            if (!totals.containsKey(job)) {
                totals.put(job, 0L);
            }
            totals.put(job, totals.get(job) + level);
        }

        StringBuilder html = new StringBuilder();
        for (String job : workers.keySet()) {
            Integer amountOfWorkers = workers.get(job);
            Long totalLevel = totals.get(job);
            html.append(Html.TABLELINE_4.parse(
                    job,
                    "" + amountOfWorkers,
                    FormatUtils.cutDecimals(MathUtils.average((int) (long) totalLevel, amountOfWorkers)),
                    "" + totalLevel)
            );
        }
        return parseContainer("", html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
