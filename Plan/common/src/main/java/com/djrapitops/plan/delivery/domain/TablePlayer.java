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
package com.djrapitops.plan.delivery.domain;

import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.gathering.domain.BaseUser;
import com.djrapitops.plan.gathering.domain.Ping;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents a player displayed on a player table on players tab or /players page.
 *
 * @author AuroraLS3
 */
public class TablePlayer implements Comparable<TablePlayer> {

    private UUID uuid;
    private String name;
    private ActivityIndex activityIndex;
    private Long activePlaytime;
    private Integer sessionCount;
    private Long registered;
    private Long lastSeen;
    private String geolocation;
    private Ping ping;
    private String nicknames;

    private boolean banned = false;

    private TablePlayer() {
    }

    public static TablePlayer.Builder builder() {
        return new TablePlayer.Builder();
    }

    public static TablePlayer.Builder builderFromBaseUser(BaseUser baseUser) {
        return new TablePlayer.Builder()
                .uuid(baseUser.getUuid())
                .name(baseUser.getName())
                .registered(baseUser.getRegistered());
    }

    public UUID getPlayerUUID() {
        return uuid;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<ActivityIndex> getCurrentActivityIndex() {
        return Optional.ofNullable(activityIndex);
    }

    public Optional<Long> getActivePlaytime() {
        return Optional.ofNullable(activePlaytime);
    }

    public Optional<Integer> getSessionCount() {
        return Optional.ofNullable(sessionCount);
    }

    public Optional<Long> getRegistered() {
        return Optional.ofNullable(registered);
    }

    public Optional<Long> getLastSeen() {
        return Optional.ofNullable(lastSeen);
    }

    public Optional<String> getGeolocation() {
        return Optional.ofNullable(geolocation);
    }

    public Ping getPing() {
        return ping;
    }

    public boolean isBanned() {
        return banned;
    }

    public String getNicknames() {
        return nicknames;
    }

    @Override
    public int compareTo(TablePlayer other) {
        // Most recent first
        return Long.compare(
                other.lastSeen != null ? other.lastSeen : 0L,
                this.lastSeen != null ? this.lastSeen : 0L
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TablePlayer)) return false;
        TablePlayer that = (TablePlayer) o;
        return activePlaytime.equals(that.activePlaytime) &&
                sessionCount.equals(that.sessionCount) &&
                registered.equals(that.registered) &&
                lastSeen.equals(that.lastSeen) &&
                name.equals(that.name) &&
                activityIndex.equals(that.activityIndex) &&
                geolocation.equals(that.geolocation) &&
                ping.equals(that.ping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, activityIndex, activePlaytime, sessionCount, registered, lastSeen, geolocation, ping);
    }

    @Override
    public String toString() {
        return "TablePlayer{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                ", activityIndex=" + activityIndex +
                ", activePlaytime=" + activePlaytime +
                ", sessionCount=" + sessionCount +
                ", registered=" + registered +
                ", lastSeen=" + lastSeen +
                ", geolocation='" + geolocation + '\'' +
                ", ping='" + ping + '\'' +
                ", banned=" + banned +
                '}';
    }

    public static class Builder {
        private final TablePlayer player;

        public Builder() {
            player = new TablePlayer();
        }

        public UUID getPlayerUUID() {
            return player.uuid;
        }

        public Builder uuid(UUID playerUUID) {
            player.uuid = playerUUID;
            return this;
        }

        public Builder name(String name) {
            player.name = name;
            return this;
        }

        public Builder banned() {
            player.banned = true;
            return this;
        }

        public Builder activityIndex(ActivityIndex activityIndex) {
            player.activityIndex = activityIndex;
            return this;
        }

        public Builder activePlaytime(long activePlaytime) {
            player.activePlaytime = activePlaytime;
            return this;
        }

        public Builder sessionCount(int count) {
            player.sessionCount = count;
            return this;
        }

        public Builder registered(long registered) {
            player.registered = registered;
            return this;
        }

        public Builder lastSeen(long lastSeen) {
            player.lastSeen = lastSeen;
            return this;
        }

        public Builder geolocation(String geolocation) {
            player.geolocation = geolocation;
            return this;
        }

        public Builder ping(Ping ping) {
            player.ping = ping;
            return this;
        }

        public Builder nicknames(String nicknames) {
            player.nicknames = nicknames;
            return this;
        }

        public TablePlayer build() {
            return player;
        }
    }
}