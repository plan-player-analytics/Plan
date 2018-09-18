package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.GeoInfoMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.utilities.comparators.PlayerContainerLastPlayedComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.Html;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.util.List;

/**
 * Html table that displays a lot of information about players.
 *
 * @author Rsl1122
 */
class PlayersTable extends TableContainer {

    private final List<PlayerContainer> players;
    private final int maxPlayers;
    private final Formatter<Double> decimalFormatter;

    PlayersTable(
            List<PlayerContainer> players,
            int maxPlayers,
            Formatter<Long> timeAmountFormatter,
            Formatter<Long> yearLongFormatter,
            Formatter<Double> decimalFormatter
    ) {
        super(
                Icon.called("user") + " Name",
                Icon.called("check") + " Activity Index",
                Icon.called("clock").of(Family.REGULAR) + " Playtime",
                Icon.called("calendar-plus").of(Family.REGULAR) + " Sessions",
                Icon.called("user-plus") + " Registered",
                Icon.called("calendar-check").of(Family.REGULAR) + " Last Seen",
                Icon.called("globe") + " Geolocation"
        );
        this.players = players;
        this.maxPlayers = maxPlayers;
        this.decimalFormatter = decimalFormatter;
        useJqueryDataTables("player-table");

        setFormatter(2, timeAmountFormatter);
        setFormatter(4, yearLongFormatter);
        setFormatter(5, yearLongFormatter);
        addRows();
    }

    private void addRows() {
        PlanAPI planAPI = PlanAPI.getInstance();
        long now = System.currentTimeMillis();

        players.sort(new PlayerContainerLastPlayedComparator());

        int i = 0;
        for (PlayerContainer player : players) {
            if (i >= maxPlayers) {
                break;
            }
            String name = player.getValue(PlayerKeys.NAME).orElse("Unknown");
            String url = planAPI.getPlayerInspectPageLink(name);

            SessionsMutator sessionsMutator = SessionsMutator.forContainer(player);
            int loginTimes = sessionsMutator.count();
            long playtime = sessionsMutator.toPlaytime();
            long registered = player.getValue(PlayerKeys.REGISTERED).orElse(0L);
            long lastSeen = sessionsMutator.toLastSeen();

            ActivityIndex activityIndex = player.getActivityIndex(now);
            boolean isBanned = player.getValue(PlayerKeys.BANNED).orElse(false);
            String activityString = activityIndex.getFormattedValue(decimalFormatter)
                    + (isBanned ? " (<b>Banned</b>)" : " (" + activityIndex.getGroup() + ")");

            String geolocation = GeoInfoMutator.forContainer(player).mostRecent().map(GeoInfo::getGeolocation).orElse("-");

            addRow(
                    Html.LINK_EXTERNAL.parse(url, name),
                    activityString,
                    playtime,
                    loginTimes,
                    registered,
                    lastSeen,
                    geolocation
            );

            i++;
        }

    }
}