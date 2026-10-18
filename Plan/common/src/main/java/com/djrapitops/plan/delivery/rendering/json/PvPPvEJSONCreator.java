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
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.KillQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public Map<String, Object> createJSONAsMap(ServerUUID serverUUID) {
        Map<String, Object> serverOverview = new HashMap<>();
        serverOverview.put("numbers", createNumbersMap(serverUUID));
        serverOverview.put("insights", createInsightsMap(serverUUID));
        return serverOverview;
    }

    private Map<String, Object> createNumbersMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long weekAgo = now - TimeUnit.DAYS.toMillis(7L);
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        Map<String, Object> numbers = new HashMap<>();
        Long pks = db.query(KillQueries.playerKillCount(0L, now, serverUUID));
        Long pks7d = db.query(KillQueries.playerKillCount(weekAgo, now, serverUUID));
        Long pks30d = db.query(KillQueries.playerKillCount(monthAgo, now, serverUUID));
        numbers.put("playerKillsTotal", pks);
        numbers.put("playerKills30d", pks30d);
        numbers.put("playerKills7d", pks7d);

        numbers.put("playerKdrAvg", decimals.apply(db.query(KillQueries.averageKDR(0L, now, serverUUID))));
        numbers.put("playerKdrAvg30d", decimals.apply(db.query(KillQueries.averageKDR(monthAgo, now, serverUUID))));
        numbers.put("playerKdrAvg7d", decimals.apply(db.query(KillQueries.averageKDR(weekAgo, now, serverUUID))));

        Long mobKills = db.query(KillQueries.mobKillCount(0L, now, serverUUID));
        Long mobKills30d = db.query(KillQueries.mobKillCount(monthAgo, now, serverUUID));
        Long mobKills7d = db.query(KillQueries.mobKillCount(weekAgo, now, serverUUID));
        numbers.put("mobKillsTotal", mobKills);
        numbers.put("mobKills30d", mobKills30d);
        numbers.put("mobKills7d", mobKills7d);

        Long deaths = db.query(KillQueries.deathCount(0L, now, serverUUID));
        Long deaths30d = db.query(KillQueries.deathCount(monthAgo, now, serverUUID));
        Long deaths7d = db.query(KillQueries.deathCount(weekAgo, now, serverUUID));
        numbers.put("deathsTotal", deaths);
        numbers.put("deaths30d", deaths30d);
        numbers.put("deaths7d", deaths7d);

        long mobDeaths = deaths - pks;
        long mobDeaths30d = deaths30d - pks30d;
        long mobDeaths7d = deaths7d - pks7d;

        numbers.put("mobDeathsTotal", mobDeaths);
        numbers.put("mobDeaths30d", mobDeaths30d);
        numbers.put("mobDeaths7d", mobDeaths7d);

        double mobKdr = mobDeaths != 0 ? (double) mobKills / mobDeaths : mobKills;
        double mobKdr30d = mobDeaths30d != 0 ? (double) mobKills30d / mobDeaths30d : mobKills30d;
        double mobKdr7d = mobDeaths7d != 0 ? (double) mobKills7d / mobDeaths7d : mobKills7d;
        numbers.put("mobKdrTotal", decimals.apply(mobKdr));
        numbers.put("mobKdr30d", decimals.apply(mobKdr30d));
        numbers.put("mobKdr7d", decimals.apply(mobKdr7d));

        return numbers;
    }

    private Map<String, Object> createInsightsMap(ServerUUID serverUUID) {
        Database db = dbSystem.getDatabase();
        long now = System.currentTimeMillis();
        long monthAgo = now - TimeUnit.DAYS.toMillis(30L);

        Map<String, Object> insights = new HashMap<>();

        List<String> top3Weapons = db.query(KillQueries.topWeaponsOfServer(monthAgo, now, serverUUID, 3));
        insights.put("weapon1st", getWeapon(top3Weapons, 0).orElse("-"));
        insights.put("weapon2nd", getWeapon(top3Weapons, 1).orElse("-"));
        insights.put("weapon3rd", getWeapon(top3Weapons, 2).orElse("-"));

        return insights;
    }

    private <T> Optional<T> getWeapon(List<T> list, int index) {
        return list.size() <= index ? Optional.empty() : Optional.of(list.get(index));
    }
}