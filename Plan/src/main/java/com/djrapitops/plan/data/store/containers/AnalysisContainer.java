package com.djrapitops.plan.data.store.containers;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.store.keys.AnalysisKeys;
import com.djrapitops.plan.data.store.keys.ServerKeys;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerProperties;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.List;

/**
 * Container used for analysis.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.store.keys.AnalysisKeys for Key objects
 * @see com.djrapitops.plan.data.store.PlaceholderKey for placeholder information
 */
public class AnalysisContainer extends DataContainer {

    private final ServerContainer serverContainer;

    public AnalysisContainer(ServerContainer serverContainer) {
        this.serverContainer = serverContainer;
        addAnalysisSuppliers();
    }

    private void addAnalysisSuppliers() {
        addConstants();
    }

    private void addConstants() {
        long now = System.currentTimeMillis();
        putRawData(AnalysisKeys.ANALYSIS_TIME, now);
        putRawData(AnalysisKeys.ANALYSIS_TIME_DAY_AGO, now - TimeAmount.DAY.ms());
        putRawData(AnalysisKeys.ANALYSIS_TIME_WEEK_AGO, now - TimeAmount.WEEK.ms());
        putRawData(AnalysisKeys.ANALYSIS_TIME_MONTH_AGO, now - TimeAmount.MONTH.ms());
        putSupplier(AnalysisKeys.REFRESH_TIME_F, () -> Formatters.second().apply(() -> getUnsafe(AnalysisKeys.ANALYSIS_TIME)));

        putRawData(AnalysisKeys.VERSION, PlanPlugin.getInstance().getVersion());
        putSupplier(AnalysisKeys.TIME_ZONE, MiscUtils::getTimeZoneOffsetHours);
        putRawData(AnalysisKeys.FIRST_DAY, 1);
        putRawData(AnalysisKeys.TPS_MEDIUM, Settings.THEME_GRAPH_TPS_THRESHOLD_MED.getNumber());
        putRawData(AnalysisKeys.TPS_HIGH, Settings.THEME_GRAPH_TPS_THRESHOLD_HIGH.getNumber());

        addServerProperties();
        addThemeColors();
        addPlayerSuppliers();
        addSessionSuppliers();
    }

    private void addServerProperties() {
        putSupplier(AnalysisKeys.SERVER_NAME, ServerInfo::getServerName);

        ServerProperties serverProperties = ServerInfo.getServerProperties();
        putRawData(AnalysisKeys.PLAYERS_MAX, serverProperties.getMaxPlayers());
        putRawData(AnalysisKeys.PLAYERS_ONLINE, serverProperties.getOnlinePlayers());
    }

    private void addThemeColors() {
        putRawData(AnalysisKeys.ACTIVITY_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE));
        putRawData(AnalysisKeys.GM_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_GM_PIE));
        putRawData(AnalysisKeys.PLAYERS_GRAPH_COLOR, Theme.getValue(ThemeVal.GRAPH_PLAYERS_ONLINE));
        putRawData(AnalysisKeys.TPS_LOW_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_LOW));
        putRawData(AnalysisKeys.TPS_MEDIUM_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_MED));
        putRawData(AnalysisKeys.TPS_HIGH_COLOR, Theme.getValue(ThemeVal.GRAPH_TPS_HIGH));
        putRawData(AnalysisKeys.WORLD_MAP_LOW_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_LOW));
        putRawData(AnalysisKeys.WORLD_MAP_HIGH_COLOR, Theme.getValue(ThemeVal.WORLD_MAP_HIGH));
        putRawData(AnalysisKeys.WORLD_PIE_COLORS, Theme.getValue(ThemeVal.GRAPH_WORLD_PIE));
    }

    private void addPlayerSuppliers() {
        putSupplier(AnalysisKeys.PLAYERS_TOTAL, () -> serverContainer.getValue(ServerKeys.PLAYER_COUNT).orElse(0));
        putSupplier(AnalysisKeys.PLAYERS_LAST_PEAK, () ->
                serverContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS)
                        .map(dateObj -> Integer.toString(dateObj.getValue())).orElse("-")
        );
        putSupplier(AnalysisKeys.PLAYERS_ALL_TIME_PEAK, () ->
                serverContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS)
                        .map(dateObj -> Integer.toString(dateObj.getValue())).orElse("-")
        );
        putSupplier(AnalysisKeys.LAST_PEAK_TIME_F, () ->
                serverContainer.getValue(ServerKeys.RECENT_PEAK_PLAYERS)
                        .map(dateObj -> Formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.ALL_TIME_PEAK_TIME_F, () ->
                serverContainer.getValue(ServerKeys.ALL_TIME_PEAK_PLAYERS)
                        .map(dateObj -> Formatters.year().apply(dateObj)).orElse("-")
        );
        putSupplier(AnalysisKeys.OPERATORS, () -> serverContainer.getValue(ServerKeys.OPERATORS).map(List::size).orElse(0));
    }

    private void addSessionSuppliers() {
        putSupplier(AnalysisKeys.SESSIONS_MUTATOR, () -> new SessionsMutator(serverContainer.getValue(ServerKeys.SESSIONS).orElse(new ArrayList<>())));
        putSupplier(AnalysisKeys.AVERAGE_SESSION_LENGTH_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toAverageSessionLength())
        );
        putSupplier(AnalysisKeys.SESSION_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).count());
        putSupplier(AnalysisKeys.PLAYTIME_TOTAL, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlaytime());
        putSupplier(AnalysisKeys.DEATHS, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toDeathCount());
        putSupplier(AnalysisKeys.MOB_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toMobKillCount());
        putSupplier(AnalysisKeys.PLAYER_KILL_COUNT, () -> getUnsafe(AnalysisKeys.SESSIONS_MUTATOR).toPlayerKillCount());
        putSupplier(AnalysisKeys.PLAYTIME_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL))
        );
        putSupplier(AnalysisKeys.AVERAGE_PLAYTIME_F, () -> Formatters.timeAmount()
                .apply(getUnsafe(AnalysisKeys.PLAYTIME_TOTAL) / (long) getUnsafe(AnalysisKeys.PLAYERS_TOTAL))
        );
    }
}