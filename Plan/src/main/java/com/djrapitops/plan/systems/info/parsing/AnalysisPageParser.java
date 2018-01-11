/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.info.parsing;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.api.IPlan;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plugin.api.Check;

import java.io.IOException;

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

        try {
            return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("web/server.html"), placeHolders);
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }

    private int getPlayersOnline() {
        if (Check.isBukkitAvailable()) {
            return ((Plan) plugin).getServer().getOnlinePlayers().size();
        }
        return ((PlanBungee) plugin).getProxy().getOnlineCount();
    }
}