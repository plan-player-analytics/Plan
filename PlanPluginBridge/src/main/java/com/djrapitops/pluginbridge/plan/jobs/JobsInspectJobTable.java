package com.djrapitops.pluginbridge.plan.jobs;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.dao.JobsDAOData;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * PluginData class for Jobs-plugin.
 * <p>
 * Registered to the plugin by JobsHook
 *
 * @author Rsl1122
 * @see JobsHook
 * @since 3.2.1
 */
public class JobsInspectJobTable extends PluginData {

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     */
    public JobsInspectJobTable() {
        super("Jobs", "inspect_table");
        super.setAnalysisOnly(false);
        final String job = Html.FONT_AWESOME_ICON.parse("suitcase") + " Job";
        final String level = Html.FONT_AWESOME_ICON.parse("plus") + " Level";
        super.setPrefix(Html.TABLE_START_2.parse(job, level));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        try {
            List<JobsDAOData> playersJobs = Jobs.getDBManager().getDB().getAllJobs(null, uuid);
            if (!playersJobs.isEmpty()) {
                StringBuilder html = new StringBuilder();
                for (JobsDAOData job : playersJobs) {
                    html.append(Html.TABLELINE_2.parse(job.getJobName(), job.getLevel()));
                }
                return parseContainer("", html.toString());
            }
        } catch (NullPointerException ignored) {
        }
        return parseContainer("", Html.TABLELINE_2.parse("No Jobs.", ""));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
