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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.file.Resource;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents values taken from a stats file for a specific player.
 *
 * @author AuroraLS3
 */
public class MinecraftStatistics {

    private final UUID playerUUID;
    private final ServerUUID serverUUID;
    private final Map<Integer, Integer> valuesById;

    public MinecraftStatistics(UUID playerUUID, ServerUUID serverUUID, Map<Integer, Integer> valuesById) {
        this.playerUUID = playerUUID;
        this.serverUUID = serverUUID;
        this.valuesById = valuesById;
    }

    public static Map<String, Integer> parse(Resource statsFile) throws IOException {
        JsonObject json = statsFile.asJson();
        JsonObject stats = json.getAsJsonObject("stats");

        Map<String, Integer> valueByKey = new HashMap<>();
        addToMap("", stats, valueByKey);
        return valueByKey;
    }

    private static void addToMap(String currentKey, JsonElement current, Map<String, Integer> valueByKey) {
        if (current.isJsonObject()) {
            for (Map.Entry<String, JsonElement> element : current.getAsJsonObject().entrySet()) {
                String key = currentKey + element.getKey().replace(':', '.');
                JsonElement value = element.getValue();
                if (value.isJsonObject()) {
                    addToMap(key + ":", value.getAsJsonObject(), valueByKey);
                } else if (value.isJsonPrimitive()) {
                    try {
                        valueByKey.put(key, value.getAsInt());
                    } catch (NumberFormatException notANumber) {
                        // Skip
                    }
                }
            }
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public ServerUUID getServerUUID() {
        return serverUUID;
    }

    public Map<Integer, Integer> getValuesById() {
        return valuesById;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftStatistics that = (MinecraftStatistics) o;
        return Objects.equals(getPlayerUUID(), that.getPlayerUUID()) && Objects.equals(getServerUUID(), that.getServerUUID()) && Objects.equals(getValuesById(), that.getValuesById());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerUUID(), getServerUUID(), getValuesById());
    }

    @Override
    public String toString() {
        return "MinecraftStatistics{" +
                "playerUUID=" + playerUUID +
                ", serverUUID=" + serverUUID +
                ", valuesById=" + valuesById +
                '}';
    }
}
