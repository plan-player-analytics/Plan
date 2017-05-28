package main.java.com.djrapitops.plan.data.additional.jobs;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.PlayerManager;
import com.gamingmesh.jobs.container.Job;
import com.gamingmesh.jobs.container.JobProgression;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MathUtils;
import static org.bukkit.Bukkit.getOfflinePlayers;

/**
 * PluginData class for Jobs-plugin.
 *
 * Registered to the plugin by JobsHook
 *
 * @author Rsl1122
 * @since 3.2.1
 * @see JobsHook
 */
public class JobsAnalysisJobTable extends PluginData {

    public JobsAnalysisJobTable() {
        super("Jobs", "analysistable", AnalysisType.HTML);
        final String job = Html.FONT_AWESOME_ICON.parse("suitcase") + " Job";
        final String workers = Html.FONT_AWESOME_ICON.parse("users") + " Workers";
        final String tLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Total Level";
        final String aLevel = Html.FONT_AWESOME_ICON.parse("plus") + " Avg Level";
        super.setPrefix(Html.TABLE_START_4.parse(job, workers, aLevel, tLevel));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        PlayerManager pm = Jobs.getPlayerManager();
        List<List<JobProgression>> players = Arrays.stream(getOfflinePlayers())
                .filter(p -> p != null)
                .map(p -> pm.getPlayerInfo(p.getUniqueId()))
                .filter(i -> i != null)
                .map(i -> pm.getJobsPlayerOffline(i))
                .map(p -> p.getJobProgression())
                .filter(list -> !list.isEmpty())
                .collect(Collectors.toList());
        if (players.isEmpty()) {
            return parseContainer("", Html.TABLELINE_4.parse("No Players with Jobs", "", "", ""));
        }
        Map<String, Integer> workers = new HashMap<>();
        Map<String, Long> totals = new HashMap<>();
        for (List<JobProgression> jobs : players) {
            for (JobProgression job : jobs) {
                String name = job.getJob().getName();
                int level = job.getLevel();
                if (!workers.containsKey(name)) {
                    workers.put(name, 0);
                }
                workers.put(name, workers.get(name) + 1);
                if (!totals.containsKey(name)) {
                    totals.put(name, 0L);
                }
                totals.put(name, totals.get(name) + level);
            }
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
