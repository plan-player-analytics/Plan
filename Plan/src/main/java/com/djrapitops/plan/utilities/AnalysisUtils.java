package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ui.graphs.GMTimesPieChartCreator;
import java.util.HashMap;
import org.bukkit.GameMode;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class AnalysisUtils {

    public static String createPieChart(HashMap<GameMode, Long> gmTimes, String uuid) {
        String url = GMTimesPieChartCreator.createChart(gmTimes, uuid);
        return "<img src=\"" + url + "\">";
    }

}
