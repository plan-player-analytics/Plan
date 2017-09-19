/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.info.parsing;

import com.djrapitops.plugin.utilities.Compatibility;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.ParseException;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.io.FileNotFoundException;

/**
 * Used for parsing a Html String out of AnalysisData and the html file.
 *
 * @author Rsl1122
 */
public class AnalysisPageParser extends PageParser {

    private final AnalysisData data;
    private final IPlan plugin;

    public AnalysisPageParser(AnalysisData analysisData, IPlan plugin) {
        this.data = analysisData;
        this.plugin = plugin;
    }

    @Override
    public String parse() throws ParseException {
        addValues(data.getReplaceMap());
        addValue("tabContentPlugins", data.replacePluginsTabLayout());
        addValue("serverName", Settings.SERVER_NAME.toString());
        addValue("timeZone", MiscUtils.getTimeZoneOffsetHours());

        addValue("playersMax", plugin.getVariable().getMaxPlayers());
        addValue("playersOnline", getPlayersOnline());
        try {
            return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("server.html"), placeHolders);
        } catch (FileNotFoundException e) {
            throw new ParseException(e);
        }
    }

    private int getPlayersOnline() {
        if (Compatibility.isBukkitAvailable()) {
            return plugin.fetch().getOnlinePlayers().size();
        }
        return ((PlanBungee) plugin).getProxy().getOnlineCount();
    }
}