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
package com.djrapitops.plan.delivery.rendering.json.graphs.special;

import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory class for different objects representing special HTML graphs.
 *
 * @author AuroraLS3
 */
@Singleton
public class SpecialGraphFactory {

    private final PlanFiles files;
    private final PlanConfig config;
    private final Gson gson;
    private final ErrorLogger errorLogger;

    private Map<String, String> geoCodes = null;

    @Inject
    public SpecialGraphFactory(PlanFiles files, PlanConfig config, Gson gson, ErrorLogger errorLogger) {
        this.files = files;
        this.config = config;
        this.gson = gson;
        this.errorLogger = errorLogger;
    }

    public PunchCard punchCard(List<FinishedSession> sessions) {
        return punchCard(new SessionsMutator(sessions));
    }

    public PunchCard punchCard(SessionsMutator sessions) {
        return new PunchCard(sessions, config.getTimeZone());
    }

    public WorldMap worldMap(Map<String, Integer> geolocationCounts) {
        if (geoCodes == null) prepareGeocodes();
        return new WorldMap(geoCodes, geolocationCounts);
    }

    private void prepareGeocodes() {
        try {
            geoCodes = files.getResourceFromJar("geocodes.json")
                    .asParsed(gson, new TypeToken<>() {});
        } catch (IOException e) {
            geoCodes = new HashMap<>();
            errorLogger.error(e, ErrorContext.builder().whatToDo("Failed to read geocodes.json from jar. Try restarting the server.").build());
        }
    }

    public Map<String, String> getGeocodes() {
        if (geoCodes == null) prepareGeocodes();
        return geoCodes;
    }

}