package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for creating recent login list html.
 * <p>
 * This item can be seen on the Information tab on Server page.
 *
 * @author Rsl1122
 */
public class RecentLoginList {

    private final List<PlayerContainer> players;

    public RecentLoginList(List<PlayerContainer> players) {
        this.players = players;
    }

    public String toHtml() {
        List<RecentLogin> recentLogins = getMostRecentLogins();

        Formatter<DateHolder> formatter = Formatters.second();

        if (recentLogins.isEmpty()) {
            return "<li>No Recent Logins</li>";
        }

        StringBuilder html = new StringBuilder();
        int i = 0;
        for (RecentLogin recentLogin : recentLogins) {
            if (i >= 20) {
                break;
            }

            String name = recentLogin.name;
            String url = PlanAPI.getInstance().getPlayerInspectPageLink(name);
            boolean isNew = recentLogin.isNew;
            String start = formatter.apply(recentLogin);

            html.append("<li><a class=\"col-").append(isNew ? "light-green" : "blue").append(" font-bold\" href=\"").append(url)
                    .append("\">").append(name).append("</a><span class=\"pull-right\">").append(start).append("</span></li>");

            i++;
        }

        return html.toString();
    }

    private List<RecentLogin> getMostRecentLogins() {
        List<RecentLogin> recentLogins = new ArrayList<>();
        for (PlayerContainer player : players) {
            if (!player.supports(PlayerKeys.NAME)
                    || !player.supports(PlayerKeys.SESSIONS)) {
                continue;
            }
            String name = player.getUnsafe(PlayerKeys.NAME);
            long registerDate = player.getValue(PlayerKeys.REGISTERED).orElse(0L);

            List<Session> sessions = player.getUnsafe(PlayerKeys.SESSIONS);
            if (sessions.isEmpty()) {
                continue;
            }
            sessions.sort(new SessionStartComparator());
            Session session = sessions.get(0);

            if (!session.supports(SessionKeys.START)) {
                continue;
            }
            long mostRecentStart = session.getUnsafe(SessionKeys.START);
            boolean isFirstSession = Math.abs(registerDate - mostRecentStart) < TimeAmount.SECOND.ms() * 10L;
            recentLogins.add(new RecentLogin(mostRecentStart, isFirstSession, name));
        }
        return recentLogins;
    }

    class RecentLogin implements DateHolder {
        final long date;
        final boolean isNew;
        final String name;

        RecentLogin(long date, boolean isNew, String name) {
            this.date = date;
            this.isNew = isNew;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof RecentLogin)) return false;
            RecentLogin that = (RecentLogin) o;
            return date == that.date &&
                    isNew == that.isNew &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(date, isNew, name);
        }

        @Override
        public long getDate() {
            return date;
        }
    }

}