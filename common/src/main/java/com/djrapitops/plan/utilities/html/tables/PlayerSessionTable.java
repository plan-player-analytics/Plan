package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.analysis.AnalysisUtils;
import com.djrapitops.plan.utilities.html.Html;

import java.util.ArrayList;
import java.util.List;

/**
 * TableContainer for a Session table for a single player.
 *
 * @author Rsl1122
 */
public class PlayerSessionTable extends TableContainer {

    private final String playerName;
    private final List<Session> sessions;

    public PlayerSessionTable(String playerName, List<Session> sessions) {
        super("Player", "Start", "Length", "World");
        this.playerName = playerName;
        this.sessions = sessions;

        addRows();
    }

    public static PlayerSessionTable forContainer(DataContainer container) {
        return new PlayerSessionTable(
                container.getValue(PlayerKeys.NAME).orElse("Unknown"),
                container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>())
        );
    }

    private void addRows() {
        int maxSessions = Settings.MAX_SESSIONS.getNumber();
        if (maxSessions <= 0) {
            maxSessions = 50;
        }

        String inspectUrl = PlanAPI.getInstance().getPlayerInspectPageLink(playerName);

        int i = 0;
        for (Session session : sessions) {
            if (i >= maxSessions) {
                break;
            }

            String start = Formatters.year().apply(session);
            String length = session.supports(SessionKeys.END)
                    ? Formatters.timeAmount().apply(session.getValue(SessionKeys.LENGTH).orElse(0L))
                    : "Online";
            String world = AnalysisUtils.getLongestWorldPlayed(session);

            String toolTip = "Session ID: " + session.getValue(SessionKeys.DB_ID)
                    .map(id -> Integer.toString(id))
                    .orElse("Not Saved.");
            addRow(Html.LINK_TOOLTIP.parse(inspectUrl, playerName, toolTip), start, length, world);

            i++;
        }
    }
}