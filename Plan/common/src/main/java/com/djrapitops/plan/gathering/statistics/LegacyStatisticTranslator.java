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

import com.djrapitops.plan.storage.file.Resource;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This utility is for translating 1.12 statistics to 26.1 equivalents.
 *
 * @author AuroraLS3
 */
public class LegacyStatisticTranslator {

    private final Map<String, String> customStats;
    private final List<Rule> rules;
    private final Set<String> unsupportedKeys;
    private final Pair<String[], String[]> entityMappings;
    private final Pair<String[], String[]> itemMappings;
    private final Pair<String[], String[]> blockMappings;

    public LegacyStatisticTranslator(
            Resource rules,
            Resource custom,
            Resource unsupported,
            Resource entityAliases,
            Resource blockAliases,
            Resource itemAliases
    ) throws IOException {
        this.rules = rules.asJson().getAsJsonArray("rules")
                .asList()
                .stream().map(JsonElement::getAsJsonObject)
                .map(Rule::new)
                .toList();
        this.customStats = custom.asJson().asMap().entrySet()
                .stream()
                .collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getAsString()));
        this.unsupportedKeys = unsupported.asJsonArray()
                .asList()
                .stream()
                .map(JsonElement::getAsString)
                .collect(Collectors.toSet());

        this.entityMappings = readMappings(entityAliases.asJson().getAsJsonObject("mappings").asMap());
        this.itemMappings = readMappings(itemAliases.asJson().getAsJsonObject("mappings").asMap());
        this.blockMappings = readMappings(blockAliases.asJson().getAsJsonObject("mappings").asMap());
    }

    private static Pair<String[], String[]> readMappings(Map<String, JsonElement> mappings) {
        String[] searchList = new String[mappings.size()];
        String[] replacementList = new String[mappings.size()];

        int i = 0;
        for (var entry : mappings.entrySet()) {
            searchList[i] = entry.getKey();
            replacementList[i] = entry.getValue().getAsString();
            i++;
        }

        return Pair.of(searchList, replacementList);
    }

    private static @NonNull String toSnakeCase(String entity) {
        return entity
                .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .toLowerCase();
    }

    public String translate(String legacyKey) {
        if (unsupportedKeys.contains(legacyKey)) {
            return legacyKey.replace("stat.", "minecraft.legacy:");
        }

        String custom = customStats.get(legacyKey);
        if (custom != null) {
            return custom;
        }

        for (Rule rule : rules) {
            Matcher matcher = rule.pattern().matcher(legacyKey);
            if (matcher.matches()) {
                String itemOrEntity = matcher.toMatchResult().group(1);
                if (Strings.CS.containsAny(legacyKey, "killEntity", "entityKilledBy")) {
                    itemOrEntity = mapEntity(itemOrEntity);
                }
                if (Strings.CS.containsAny(legacyKey, "mineBlock", "craftItem", "useItem", "breakItem", "pickup", "drop")) {
                    itemOrEntity = mapItem(itemOrEntity);
                }

                return rule.replacement.replace("$1", itemOrEntity);
            }
        }

        return legacyKey;
    }

    private String mapItem(String item) {
        String translatedKey = StringUtils.replaceEach(item, itemMappings.getLeft(), itemMappings.getRight());
        translatedKey = StringUtils.replaceEach(translatedKey, blockMappings.getLeft(), blockMappings.getRight());
        // Ambiguous blocks/items are left as-is, they are still listed in the json.
        return translatedKey;
    }

    private String mapEntity(String entity) {
        String translatedKey = StringUtils.replaceEach(entity, entityMappings.getLeft(), entityMappings.getRight());
        if (entity.equals(translatedKey)) {
            return "minecraft:" + toSnakeCase(entity);
        }
        return translatedKey;
    }

    private record Rule(Pattern pattern, String replacement) {
        public Rule(JsonObject rule) {
            this(Pattern.compile(rule.get("pattern").getAsString()),
                    rule.get("replacement").getAsString());
        }
    }

    private record Mapping(String key, String replacement) {
        public Mapping(Map.Entry<String, JsonElement> entry) {
            this(entry.getKey(), entry.getValue().getAsString());
        }
    }
}

