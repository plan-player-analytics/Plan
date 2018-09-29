package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Html table that can be used to replace a {@link com.djrapitops.plan.utilities.html.structure.SessionAccordion}.
 *
 * @author Rsl1122
 */
class ServerSessionTable extends TableContainer {

    private final int maxSessions;
    private final WorldAliasSettings worldAliasSettings;
    private final Formatter<DateHolder> yearFormatter;
    private final Formatter<Long> timeAmountFormatter;

    private final List<Session> sessions;
    private Map<UUID, String> playerNames;

    ServerSessionTable(
            Map<UUID, String> playerNames, List<Session> sessions,
            int maxSessions,
            WorldAliasSettings worldAliasSettings,
            Formatter<DateHolder> yearFormatter,
            Formatter<Long> timeAmountFormatter
    ) {
        super("Player", "Start", "Length", "World");
        this.playerNames = playerNames;
        this.sessions = sessions;
        this.maxSessions = maxSessions;
        this.worldAliasSettings = worldAliasSettings;
        this.yearFormatter = yearFormatter;
        this.timeAmountFormatter = timeAmountFormatter;

        addRows();
    }

    private void addRows() {
        int i = 0;
        for (Session session : sessions) {
            if (i >= maxSessions) {
                break;
            }

            String start = yearFormatter.apply(session);
            String length = session.supports(SessionKeys.END)
                    ? timeAmountFormatter.apply(session.getValue(SessionKeys.LENGTH).orElse(0L))
                    : "Online";
            String world = worldAliasSettings.getLongestWorldPlayed(session);

            String toolTip = "Session ID: " + session.getValue(SessionKeys.DB_ID)
                    .map(id -> Integer.toString(id))
                    .orElse("Not Saved.");

            String playerName = playerNames.getOrDefault(session.getValue(SessionKeys.UUID).orElse(null), "Unknown");
            String inspectUrl = PlanAPI.getInstance().getPlayerInspectPageLink(playerName);

            addRow(Html.LINK_TOOLTIP.parse(inspectUrl, playerName, toolTip), start, length, world);

            i++;
        }
    }
}