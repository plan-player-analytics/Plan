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

import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.identification.ServerUUID;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ActiveSession {

    private final UUID playerUUID;
    private final ServerUUID serverUUID;
    private final long start;
    private final DataMap extraData;
    private long afkTime;

    private long lastMovementForAfkCalculation;

    public ActiveSession(UUID playerUUID, ServerUUID serverUUID, long start, String world, String gameMode) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.start = start;
        this.afkTime = 0L;

        extraData = new DataMap();
        extraData.put(WorldTimes.class, new WorldTimes(world, gameMode, start));
        extraData.put(MobKillCounter.class, new MobKillCounter());
        extraData.put(DeathCounter.class, new DeathCounter());
        extraData.put(PlayerKills.class, new PlayerKills());

        lastMovementForAfkCalculation = start;
    }

    public static ActiveSession fromPlayerJoin(PlayerJoin join) {
        return new ActiveSession(join.getPlayerUUID(), join.getServerUUID(), join.getTime(),
                join.getPlayerMetadata().getWorld().orElse("Unspecified"),
                join.getPlayerMetadata().getGameMode().orElse("Unknown"));
    }

    public FinishedSession toFinishedSessionFromStillActive() {
        updateState();
        FinishedSession finishedSession = toFinishedSession(System.currentTimeMillis());
        finishedSession.getExtraData().put(ActiveSession.class, this);
        return finishedSession;
    }

    public FinishedSession toFinishedSession(long end) {
        updateState(end);
        return new FinishedSession(playerUUID, serverUUID, start, end, afkTime, extraData.copy());
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

    public void addAfkTime(long time) {
        afkTime += time;
    }

    public void addDeath() {
        extraData.get(DeathCounter.class).ifPresent(Counter::add);
    }

    public void addMobKill() {
        extraData.get(MobKillCounter.class).ifPresent(Counter::add);
    }

    public void addPlayerKill(PlayerKill kill) {
        extraData.get(PlayerKills.class).ifPresent(kills -> kills.add(kill));
    }

    public void setAsFirstSessionIfMatches(Long registerDate) {
        if (registerDate != null && Math.abs(start - registerDate) < TimeUnit.SECONDS.toMillis(15L)) {
            extraData.put(FirstSession.class, new FirstSession());
        }
    }

    public DataMap getExtraData() {
        return extraData;
    }

    public void updateState() {
        updateState(System.currentTimeMillis());
    }

    public void updateState(long time) {
        extraData.get(WorldTimes.class).ifPresent(times -> times.updateState(time));
    }

    public void changeState(String world, String gameMode, long time) {
        extraData.get(WorldTimes.class).ifPresent(times -> times.updateState(world, gameMode, time));
    }

    public <T> Optional<T> getExtraData(Class<T> ofType) {
        return getExtraData().get(ofType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActiveSession that = (ActiveSession) o;
        return start == that.start && afkTime == that.afkTime &&
                Objects.equals(playerUUID, that.playerUUID) &&
                Objects.equals(serverUUID, that.serverUUID) &&
                Objects.equals(getExtraData(WorldTimes.class), that.getExtraData(WorldTimes.class)) &&
                Objects.equals(getExtraData(PlayerKills.class), that.getExtraData(PlayerKills.class)) &&
                Objects.equals(getExtraData(MobKillCounter.class), that.getExtraData(MobKillCounter.class)) &&
                Objects.equals(getExtraData(DeathCounter.class), that.getExtraData(DeathCounter.class));
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID, serverUUID, start, afkTime, extraData);
    }

    @Override
    public String toString() {
        return "ActiveSession{" +
                "playerUUID=" + playerUUID +
                ", serverUUID=" + serverUUID +
                ", start=" + start +
                ", afkTime=" + afkTime +
                ", extraData=" + extraData +
                '}';
    }

    public long getLastMovementForAfkCalculation() {
        return lastMovementForAfkCalculation;
    }

    public void setLastMovementForAfkCalculation(long lastMovementForAfkCalculation) {
        this.lastMovementForAfkCalculation = lastMovementForAfkCalculation;
    }

    public static class FirstSession {}
}
