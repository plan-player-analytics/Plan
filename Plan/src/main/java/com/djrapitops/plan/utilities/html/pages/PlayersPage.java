package com.djrapitops.plan.utilities.html.pages;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.utilities.formatting.PlaceholderReplacer;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.tables.PlayersTable;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.benchmarking.Timings;

import java.util.List;

/**
 * Html String parser for /players page.
 *
 * @author Rsl1122
 */
public class PlayersPage implements Page {

    private final String version;
    private final PlanConfig config;
    private final Database database;
    private final ServerInfo serverInfo;

    private final Timings timings;

    PlayersPage(
            String version,
            PlanConfig config,
            Database database,
            ServerInfo serverInfo,
            Timings timings
    ) {
        this.version = version;
        this.config = config;
        this.database = database;
        this.serverInfo = serverInfo;
        this.timings = timings;
    }

    @Override
    public String toHtml() throws ParseException {
        try {
            PlaceholderReplacer placeholderReplacer = new PlaceholderReplacer();

            placeholderReplacer.put("version", version);
            if (Check.isBukkitAvailable()) {
                placeholderReplacer.put("networkName", serverInfo.getServer().getName());
            } else {
                placeholderReplacer.put("networkName", config.getString(Settings.BUNGEE_NETWORK_NAME));
            }

            timings.start("Players page players table parsing");
            List<PlayerContainer> playerContainers = database.fetch().getAllPlayerContainers();
            placeholderReplacer.put("playersTable", PlayersTable.forPlayersPage(playerContainers).parseHtml());
            timings.end("Pages", "Players page players table parsing");

            return placeholderReplacer.apply(FileUtil.getStringFromResource("web/players.html"));
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}