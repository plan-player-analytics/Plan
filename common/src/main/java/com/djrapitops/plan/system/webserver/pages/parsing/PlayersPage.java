package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.mutators.formatting.PlaceholderReplacer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.tables.PlayersTable;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;

import java.util.List;

/**
 * Html String parser for /players page.
 *
 * @author Rsl1122
 */
public class PlayersPage implements Page {

    @Override
    public String toHtml() throws ParseException {
        try {
            Database database = Database.getActive();
            PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();

            placeholderReplacer.put("version", PlanHelper.getInstance().getVersion());
            if (Check.isBukkitAvailable()) {
                placeholderReplacer.put("networkName", ServerInfo.getServerName());
            } else {
                placeholderReplacer.put("networkName", Settings.BUNGEE_NETWORK_NAME.toString());
            }

            Benchmark.start("Players page players table parsing");
            List<PlayerContainer> playerContainers = database.fetch().getAllPlayerContainers();
            placeholderReplacer.put("playersTable", PlayersTable.forPlayersPage(playerContainers).parseHtml());
            Log.debug(Benchmark.stopAndFormat("Players page players table parsing"));

            return placeholderReplacer.apply(FileUtil.getStringFromResource("web/players.html"));
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}