
package com.djrapitops.plan.ui;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.utilities.AnalysisUtils;
import com.djrapitops.plan.utilities.FormatUtils;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import org.bukkit.GameMode;

/**
 *
 * @author Rsl1122
 */
public class DataRequestHandler {
    private Plan plugin;
    private InspectCacheHandler inspectCache;
    private AnalysisCacheHandler analysisCache;

    public DataRequestHandler(Plan plugin) {
        this.plugin = plugin;
        this.inspectCache = plugin.getInspectCache();
        this.analysisCache = plugin.getAnalysisCache();
    }

    public boolean checkIfCached(UUID uuid) {
        return inspectCache.getCache().containsKey(uuid);
    }

    public String getDataHtml(UUID uuid) {
        UserData data = inspectCache.getFromCache(uuid);
        if (data == null) {
            return "<h1>404 Data was not found in cache</h1>";
        }
        Scanner scanner = new Scanner(plugin.getResource("player.html"));
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("%uuid%", ""+data.getUuid());
        replaceMap.put("%logintimes%", ""+data.getLoginTimes());
        replaceMap.put("%bed%", FormatUtils.formatLocation(data.getBedLocation()));
        replaceMap.put("%geoloc%", data.getDemData().getGeoLocation());
        int age = data.getDemData().getAge();
        replaceMap.put("%age%", (age != -1) ? ""+age:"Not known");
        replaceMap.put("%gender%", ""+data.getDemData().getGender().name().toLowerCase());
        HashMap<GameMode, Long> gmTimes = data.getGmTimes();
        replaceMap.put("%gmpiechart%", AnalysisUtils.createPieChart(gmTimes, data.getUuid().toString()));
        long gmZero = gmTimes.get(GameMode.SURVIVAL);
        long gmOne = gmTimes.get(GameMode.CREATIVE);
        long gmTwo = gmTimes.get(GameMode.ADVENTURE);
        long gmThree = gmTimes.get(GameMode.SPECTATOR);
        long total = gmZero + gmOne + gmTwo + gmThree;
        replaceMap.put("%gm0%", FormatUtils.formatTimeAmount(""+gmZero));
        replaceMap.put("%gm1%", FormatUtils.formatTimeAmount(""+gmOne));
        replaceMap.put("%gm2%", FormatUtils.formatTimeAmount(""+gmTwo));
        replaceMap.put("%gm3%", FormatUtils.formatTimeAmount(""+gmThree));
        replaceMap.put("%gmtotal%", FormatUtils.formatTimeAmount(""+total));
        replaceMap.put("%ips%", data.getIps().toString());
        replaceMap.put("%nicknames%", data.getNicknames().toString());
        replaceMap.put("%name%", data.getName());
        replaceMap.put("%registered%", FormatUtils.formatTimeStamp(""+data.getRegistered()));
        replaceMap.put("%timeskicked%", ""+data.getTimesKicked());
        replaceMap.put("%playtime%", FormatUtils.formatTimeAmount(""+data.getPlayTime()));
        replaceMap.put("%banned%", data.isBanned() ? "Banned":"Not Banned");
        replaceMap.put("%op%", data.isOp() ? ", Operator (Op)":"");
        
        for (String key : replaceMap.keySet()) {
            html = html.replaceAll(key, replaceMap.get(key));
        }
        
        return html;
    }

    public String getAnalysisHtml() {
        return "Test Successful";
    }

    public boolean checkIfAnalysisIsCached() {
        return analysisCache.isCached();
    }
}
