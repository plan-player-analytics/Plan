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

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.utilities.java.OptionalArray;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FinishedSession implements DateHolder {

    private final UUID playerUUID;
    private final ServerUUID serverUUID;
    private final long start;
    private final long end;

    private final long afkTime;
    private final DataMap extraData;

    public FinishedSession(
            UUID playerUUID, ServerUUID serverUUID,
            long start, long end, long afkTime,
            DataMap extraData
    ) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.start = start;
        this.end = end;
        this.afkTime = afkTime;
        this.extraData = extraData != null ? extraData : new DataMap();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public long getAfkTime() {
        return afkTime;
    }

    public long getLength() {
        return end - start;
    }

    public long getActiveTime() {
        return getLength() - getAfkTime();
    }

    public int getMobKillCount() {
        return extraData.get(MobKillCounter.class).map(MobKillCounter::getCount).orElse(0);
    }

    public int getDeathCount() {
        return extraData.get(DeathCounter.class).map(DeathCounter::getCount).orElse(0);
    }

    public int getPlayerKillCount() {
        return extraData.get(PlayerKills.class).map(PlayerKills::asList).map(List::size).orElse(0);
    }

    public void setAsFirstSessionIfMatches(Long registerDate) {
        if (registerDate != null && Math.abs(start - registerDate) < TimeUnit.SECONDS.toMillis(15L)) {
            extraData.put(ActiveSession.FirstSession.class, new ActiveSession.FirstSession());
        }
    }

    public boolean isFirstSession() {
        return extraData.get(ActiveSession.FirstSession.class).isPresent();
    }

    public DataMap getExtraData() {
        return extraData;
    }

    public <T> Optional<T> getExtraData(Class<T> ofType) {
        return getExtraData().get(ofType);
    }

    @Override
    public long getDate() {
        return getStart();
    }

    /**
     * Deserialize csv format of the session.
     *
     * @param serialized Serialized version of the session
     * @return Proper session if the csv had 9 columns or more
     * @throws com.google.gson.JsonSyntaxException if serialized format has a json syntax error
     */
    public static Optional<FinishedSession> deserializeCSV(String serialized) {
        String[] array = StringUtils.split(serialized, ';');
        OptionalArray<String> asOptionals = OptionalArray.of(array);
        if (array.length < 5) return Optional.empty();
        // Note for the future: Use length to determine version of serialized class

        Gson gson = new Gson();

        UUID playerUUID = UUID.fromString(array[0]);
        ServerUUID serverUUID = ServerUUID.fromString(array[1]);
        long start = Long.parseLong(array[2]);
        long end = Long.parseLong(array[3]);
        long afkTime = Long.parseLong(array[4]);

        DataMap extraData = new DataMap();
        asOptionals.get(5).ifPresent(value -> extraData.put(WorldTimes.class, gson.fromJson(value, WorldTimes.class)));
        asOptionals.get(6).ifPresent(value -> extraData.put(PlayerKills.class, gson.fromJson(value, PlayerKills.class)));
        asOptionals.get(7).ifPresent(value -> extraData.put(MobKillCounter.class, gson.fromJson(value, MobKillCounter.class)));
        asOptionals.get(8).ifPresent(value -> extraData.put(DeathCounter.class, gson.fromJson(value, DeathCounter.class)));
        asOptionals.get(9).ifPresent(value -> extraData.put(JoinAddress.class, new JoinAddress(value)));
        asOptionals.get(10).ifPresent(value -> extraData.put(PlayerName.class, new PlayerName(value)));
        return Optional.of(new FinishedSession(playerUUID, serverUUID, start, end, afkTime, extraData));
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID, serverUUID, start, end, afkTime, extraData);
    }

    @Override
    public String toString() {
        return "FinishedSession{" +
                "playerUUID=" + playerUUID +
                ", serverUUID=" + serverUUID +
                ", start=" + start +
                ", end=" + end +
                ", afkTime=" + afkTime +
                ", extraData=" + extraData +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FinishedSession that = (FinishedSession) o;
        return start == that.start && end == that.end &&
                afkTime == that.afkTime &&
                Objects.equals(playerUUID, that.playerUUID) &&
                Objects.equals(serverUUID, that.serverUUID) &&
                Objects.equals(getExtraData(WorldTimes.class), that.getExtraData(WorldTimes.class)) &&
                Objects.equals(getExtraData(PlayerKills.class), that.getExtraData(PlayerKills.class)) &&
                Objects.equals(getExtraData(MobKillCounter.class), that.getExtraData(MobKillCounter.class)) &&
                Objects.equals(getExtraData(DeathCounter.class), that.getExtraData(DeathCounter.class)) &&
                Objects.equals(getExtraData(JoinAddress.class), that.getExtraData(JoinAddress.class));
    }

    /**
     * Serialize into csv format.
     *
     * @return Serialized format
     */
    public String serializeCSV() {
        return (String.valueOf(playerUUID) + ';' +
                serverUUID + ';' +
                start + ';' +
                end + ';' +
                afkTime + ';' +
                getExtraData(WorldTimes.class).orElseGet(WorldTimes::new).toJson() + ';' +
                getExtraData(PlayerKills.class).orElseGet(PlayerKills::new).toJson() + ';' +
                getExtraData(MobKillCounter.class).orElseGet(MobKillCounter::new).toJson() + ';' +
                getExtraData(DeathCounter.class).orElseGet(DeathCounter::new).toJson() + ';' +
                // Join address contains @Untrusted data so possible ; needs to be neutralized
                getExtraData(JoinAddress.class).map(JoinAddress::getAddress)
                        .map(address -> address.replace(';', ':'))
                        .orElse(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP) + ';' +
                getExtraData(PlayerName.class).map(PlayerName::get).orElseGet(playerUUID::toString))
                .replace('\\', '_'); // Untrusted data can contain escape characters that fail json serialization.
    }

    public static class Id {
        private final int value;

        public Id(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }
    }
}
