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
package com.djrapitops.plan.gathering.statistics;

import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import com.google.gson.JsonElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.djrapitops.plan.storage.database.sql.tables.StatisticTable.MAX_STATISTIC_NAME_LENGTH;

/**
 * Statistics parser that also maps the keys to newer ones.
 *
 * @author AuroraLS3
 */
@Singleton
public class MinecraftStatisticsParser {

    private final PlanFiles files;

    private LegacyStatisticTranslator legacyStatisticTranslator;

    @Inject
    public MinecraftStatisticsParser(PlanFiles files) {
        this.files = files;
    }

    @VisibleForTesting
    public MinecraftStatisticsParser(LegacyStatisticTranslator legacyStatisticTranslator) {
        this.files = null;
        this.legacyStatisticTranslator = legacyStatisticTranslator;
    }

    private static void addToMap(String currentKey,
                                 JsonElement current,
                                 Map<String, Integer> valueByKey,
                                 UnaryOperator<String> keyMapper) {
        if (current == null || !current.isJsonObject()) return;

        for (var element : current.getAsJsonObject().entrySet()) {
            var key = currentKey + keyMapper.apply(element.getKey())
                    .replace("minecraft:", "minecraft.");
            var value = element.getValue();
            if (value.isJsonObject()) {
                addToMap(key + ":", value.getAsJsonObject(), valueByKey, keyMapper);
            } else if (value.isJsonPrimitive()) {
                try {
                    String truncatedKey = StringUtils.truncate(key, MAX_STATISTIC_NAME_LENGTH);
                    int number = value.getAsInt();
                    // Legacy key mapping can map two blocks to same one
                    valueByKey.compute(truncatedKey, (k, currentValue) ->
                            (currentValue == null ? 0 : currentValue) + number);
                } catch (NumberFormatException notANumber) {
                    // Skip
                }
            }
        }
    }

    public Map<String, Integer> parse(Resource statsFile) throws IOException {
        JsonElement json = statsFile.asJson();
        // Support both new format with "stats" wrapper and old flat format
        JsonElement stats;
        Map<String, Integer> valueByKey = new HashMap<>();
        if (json.isJsonObject() && json.getAsJsonObject().has("stats")) {
            stats = json.getAsJsonObject().get("stats");
            addToMap("", stats, valueByKey, UnaryOperator.identity());
        } else {
            if (legacyStatisticTranslator == null) {
                legacyStatisticTranslator = new LegacyStatisticTranslator(
                        files.getResourceFromJar("mappings/stats/rules.json"),
                        files.getResourceFromJar("mappings/stats/custom.json"),
                        files.getResourceFromJar("mappings/stats/unsupported.json"),
                        files.getResourceFromJar("mappings/stats/entity_aliases.json"),
                        files.getResourceFromJar("mappings/stats/block_aliases.json"),
                        files.getResourceFromJar("mappings/stats/item_aliases.json")
                );
            }
            // Old format: the root object contains the statistics directly
            addToMap("", json, valueByKey, legacyStatisticTranslator::translate);
        }

        return valueByKey;
    }
}
