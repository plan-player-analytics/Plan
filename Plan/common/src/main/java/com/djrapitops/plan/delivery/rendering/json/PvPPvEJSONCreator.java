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
package com.djrapitops.plan.delivery.rendering.json;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Creates JSON payload for /server-page PvP and PvE tab.
 *
 * @author AuroraLS3
 */
@Singleton
public class PvPPvEJSONCreator implements ServerTabJSONCreator<Map<String, Object>> {

    private final DBSystem dbSystem;

    private final Formatter<Double> decimals;

    @Inject
    public PvPPvEJSONCreator(
            DBSystem dbSystem,
            Formatters formatters
    ) {
        this.dbSystem = dbSystem;

        decimals = formatters.decimals();
    }

    public Map<String, Object> createJSONAsMap(UUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("numbers", createNumbersMap(serverUUID));
        serverOverview.put("insights", createInsightsMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createNumbersMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        Map<String, Object> numbers = new HashMap<>();
        Long pks = db.query(KillQueries.playerKillCount(0L, now, serverUUID));
        Long pks7d = db.query(KillQueries.playerKillCount(weekAgo, now, serverUUID));
        Long pks30d = db.query(KillQueries.playerKillCount(monthAgo, now, serverUUID));
        numbers.put("player_kills_total", pks);
        numbers.put("player_kills_30d", pks30d);
        numbers.put("player_kills_7d", pks7d);

        numbers.put("player_kdr_avg", decimals.apply(db.query(KillQueries.averageKDR(0L, now, serverUUID))));
        numbers.put("player_kdr_avg_30d", decimals.apply(db.query(KillQueries.averageKDR(monthAgo, now, serverUUID))));
        numbers.put("player_kdr_avg_7d", decimals.apply(db.query(KillQueries.averageKDR(weekAgo, now, serverUUID))));

        Long mobKills = db.query(KillQueries.mobKillCount(0L, now, serverUUID));
        Long mobKills30d = db.query(KillQueries.mobKillCount(monthAgo, now, serverUUID));
        Long mobKills7d = db.query(KillQueries.mobKillCount(weekAgo, now, serverUUID));
        numbers.put("mob_kills_total", mobKills);
        numbers.put("mob_kills_30d", mobKills30d);
        numbers.put("mob_kills_7d", mobKills7d);

        Long deaths = db.query(KillQueries.deathCount(0L, now, serverUUID));
        Long deaths30d = db.query(KillQueries.deathCount(monthAgo, now, serverUUID));
        Long deaths7d = db.query(KillQueries.deathCount(weekAgo, now, serverUUID));
        numbers.put("deaths_total", deaths);
        numbers.put("deaths_30d", deaths30d);
        numbers.put("deaths_7d", deaths7d);

        long mobDeaths = deaths - pks;
        long mobDeaths30d = deaths30d - pks30d;
        long mobDeaths7d = deaths7d - pks7d;

        numbers.put("mob_deaths_total", mobDeaths);
        numbers.put("mob_deaths_30d", mobDeaths30d);
        numbers.put("mob_deaths_7d", mobDeaths7d);

        double mobKdr = mobDeaths != 0 ? (double) mobKills / mobDeaths : mobKills;
        double mobKdr30d = mobDeaths30d != 0 ? (double) mobKills30d / mobDeaths30d : mobKills30d;
        double mobKdr7d = mobDeaths7d != 0 ? (double) mobKills7d / mobDeaths7d : mobKills7d;
        numbers.put("mob_kdr_total", decimals.apply(mobKdr));
        numbers.put("mob_kdr_30d", decimals.apply(mobKdr30d));
        numbers.put("mob_kdr_7d", decimals.apply(mobKdr7d));

        return numbers;
    }

    private Map<String, Object> createInsightsMap(UUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        Map<String, Object> insights = new HashMap<>();

        List<String> top3Weapons = db.query(KillQueries.topWeaponsOfServer(monthAgo, now, serverUUID, 3));
        insights.put("weapon_1st", getWeapon(top3Weapons, 0).orElse("-"));
        insights.put("weapon_2nd", getWeapon(top3Weapons, 1).orElse("-"));
        insights.put("weapon_3rd", getWeapon(top3Weapons, 2).orElse("-"));

        return insights;
    }

    private <T> Optional<T> getWeapon(List<T> list, int index) {
        return list.size() <= index ? Optional.empty() : Optional.of(list.get(index));
    }
}