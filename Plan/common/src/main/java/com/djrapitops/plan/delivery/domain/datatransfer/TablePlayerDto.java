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
 *  aLong with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.domain.datatransfer;

import com.djrapitops.plan.delivery.domain.datatransfer.extension.ExtensionValueDataDto;
import com.djrapitops.plan.gathering.domain.Ping;

import java.util.Map;
import java.util.UUID;

/**
 * Represents a row for players table.
 *
 * @author AuroraLS3
 */
public class TablePlayerDto {

    private UUID playerUUID;
    private String playerName;
    private double activityIndex;
    private Long playtimeActive;
    private Long sessionCount;
    private Long lastSeen;
    private Long registered;
    private String country;
    private Double pingAverage;
    private Integer pingMax;
    private Integer pingMin;

    private Map<String, ExtensionValueDataDto> extensionValues;

    private TablePlayerDto() {
        // Builder constructor
    }

    public static TablePlayerDtoBuilder builder() {
        return new TablePlayerDtoBuilder();
    }

    public String getPlayerName() {
        return playerName;
    }

    private void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public double getActivityIndex() {
        return activityIndex;
    }

    private void setActivityIndex(double activityIndex) {
        this.activityIndex = activityIndex;
    }

    public Long getPlaytimeActive() {
        return playtimeActive;
    }

    private void setPlaytimeActive(Long playtimeActive) {
        this.playtimeActive = playtimeActive;
    }

    public Long getSessionCount() {
        return sessionCount;
    }

    private void setSessionCount(Long sessionCount) {
        this.sessionCount = sessionCount;
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    private void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Long getRegistered() {
        return registered;
    }

    private void setRegistered(Long registered) {
        this.registered = registered;
    }

    public String getCountry() {
        return country;
    }

    private void setCountry(String country) {
        this.country = country;
    }

    public Map<String, ExtensionValueDataDto> getExtensionValues() {
        return extensionValues;
    }

    private void setExtensionValues(Map<String, ExtensionValueDataDto> extensionValues) {
        this.extensionValues = extensionValues;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public Double getPingAverage() {
        return pingAverage;
    }

    public void setPingAverage(Double pingAverage) {
        this.pingAverage = pingAverage;
    }

    public Integer getPingMax() {
        return pingMax;
    }

    public void setPingMax(Integer pingMax) {
        this.pingMax = pingMax;
    }

    public Integer getPingMin() {
        return pingMin;
    }

    public void setPingMin(Integer pingMin) {
        this.pingMin = pingMin;
    }

    public static final class TablePlayerDtoBuilder {
        private final TablePlayerDto tablePlayerDto;

        private TablePlayerDtoBuilder() {tablePlayerDto = new TablePlayerDto();}

        public TablePlayerDtoBuilder withUuid(UUID playerUUID) {
            tablePlayerDto.setPlayerUUID(playerUUID);
            return this;
        }

        public TablePlayerDtoBuilder withName(String name) {
            tablePlayerDto.setPlayerName(name);
            return this;
        }

        public TablePlayerDtoBuilder withActivityIndex(double activityIndex) {
            tablePlayerDto.setActivityIndex(activityIndex);
            return this;
        }

        public TablePlayerDtoBuilder withPlaytimeActive(Long playtimeActive) {
            tablePlayerDto.setPlaytimeActive(playtimeActive);
            return this;
        }

        public TablePlayerDtoBuilder withSessionCount(Long sessionCount) {
            tablePlayerDto.setSessionCount(sessionCount);
            return this;
        }

        public TablePlayerDtoBuilder withLastSeen(Long lastSeen) {
            tablePlayerDto.setLastSeen(lastSeen);
            return this;
        }

        public TablePlayerDtoBuilder withRegistered(Long registered) {
            tablePlayerDto.setRegistered(registered);
            return this;
        }

        public TablePlayerDtoBuilder withCountry(String country) {
            tablePlayerDto.setCountry(country);
            return this;
        }

        public TablePlayerDtoBuilder withExtensionValues(Map<String, ExtensionValueDataDto> extensionValues) {
            tablePlayerDto.setExtensionValues(extensionValues);
            return this;
        }

        public TablePlayerDtoBuilder withPing(Ping ping) {
            if (ping != null) {
                tablePlayerDto.setPingAverage(ping.getAverage());
                tablePlayerDto.setPingMax(ping.getMax());
                tablePlayerDto.setPingMin(ping.getMin());
            }
            return this;
        }

        public TablePlayerDto build() {return tablePlayerDto;}
    }
}
