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
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plan.delivery.domain.mutators.PlayerKillMutator;
import com.djrapitops.plan.utilities.comparators.DateHolderRecentComparator;
import org.apache.commons.text.TextStringBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class PlayerKills {

    private final List<PlayerKill> kills;

    public PlayerKills() {
        this(new ArrayList<>());
    }

    public PlayerKills(List<PlayerKill> kills) {
        this.kills = kills;
        kills.sort(new DateHolderRecentComparator());
    }

    public void add(PlayerKill kill) {
        kills.add(kill);
        kills.sort(new DateHolderRecentComparator());
    }

    public List<PlayerKill> asList() {
        return kills;
    }

    public PlayerKillMutator asMutator() {
        return new PlayerKillMutator(asList());
    }

    public void addAll(Collection<PlayerKill> randomKills) {
        this.kills.addAll(randomKills);
        kills.sort(new DateHolderRecentComparator());
    }

    public boolean contains(PlayerKill newKill) {
        return kills.contains(newKill);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerKills that = (PlayerKills) o;
        return Objects.equals(kills, that.kills);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kills);
    }

    @Override
    public String toString() {
        return "PlayerKills{" +
                "kills=" + kills +
                '}';
    }

    public String toJson() {
        return "{" +
                "  \"kills\": [" +
                new TextStringBuilder().appendWithSeparators(kills
                        .stream().map(PlayerKill::toJson).iterator(), ",").get() +
                "  ]" +
                "}";
    }
}
