/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.html.structure;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.utilities.comparators.SessionStartComparator;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for creating recent login list html.
 * <p>
 * This item can be seen on the Information tab on Server page.
 *
 * @author Rsl1122
 */
public class RecentLoginList {

    private final List<PlayerContainer> players;

    private final Formatter<Long> secondLongFormatter;

    public RecentLoginList(List<PlayerContainer> players, Formatter<Long> secondLongFormatter) {
        this.players = players;
        this.secondLongFormatter = secondLongFormatter;
    }

    public String toHtml() {
        List<RecentLogin> recentLogins = getMostRecentLogins();

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
            String start = secondLongFormatter.apply(recentLogin.date);

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

            List<Session> sessions = player.getValue(PlayerKeys.SESSIONS).orElse(Collections.emptyList());
            if (sessions.isEmpty()) {
                continue;
            }
            sessions.sort(new SessionStartComparator());
            Session session = sessions.get(0);

            if (!session.supports(SessionKeys.START)) {
                continue;
            }
            long mostRecentStart = session.getUnsafe(SessionKeys.START);
            boolean isFirstSession = Math.abs(registerDate - mostRecentStart) < TimeUnit.SECONDS.toMillis(10L);
            recentLogins.add(new RecentLogin(mostRecentStart, isFirstSession, name));
        }
        return recentLogins;
    }

    class RecentLogin {
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
    }

}