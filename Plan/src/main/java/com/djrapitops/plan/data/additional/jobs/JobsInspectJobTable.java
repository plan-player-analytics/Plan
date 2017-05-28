package main.java.com.djrapitops.plan.data.additional.jobs;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.PlayerManager;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.gamingmesh.jobs.container.PlayerInfo;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;

/**
 * PluginData class for Jobs-plugin.
 *
 * Registered to the plugin by JobsHook
 *
 * @author Rsl1122
 * @since 3.2.1
 * @see JobsHook
 */
public class JobsInspectJobTable extends PluginData {

    public JobsInspectJobTable() {
        super("Jobs", "inspecttable");
        super.setAnalysisOnly(false);
        final String job = Html.FONT_AWESOME_ICON.parse("suitcase") + " Job";
        final String level = Html.FONT_AWESOME_ICON.parse("plus") + " Level";
        super.setPrefix(Html.TABLE_START_2.parse(job, level));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        PlayerManager pm = Jobs.getPlayerManager();
        PlayerInfo info = pm.getPlayerInfo(uuid);
        JobsPlayer player = pm.getJobsPlayerOffline(info);
        List<JobProgression> progression = player.getJobProgression();
        if (progression.isEmpty()) {
            return parseContainer("", Html.TABLELINE_2.parse("No Jobs.", ""));
        }
        StringBuilder html = new StringBuilder();
        for (JobProgression job : progression) {
            html.append(Html.TABLELINE_2.parse(job.getJob().getName(), "" + job.getLevel()));
        }
        return parseContainer("", html.toString());
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
