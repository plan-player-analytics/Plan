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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.rendering.html.Contributors;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.java.Maps;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class MetadataJSONResolver implements NoAuthResolver {

    private final PlanConfig config;
    private final Theme theme;

    @Inject
    public MetadataJSONResolver(PlanConfig config, Theme theme) {
        this.config = config;
        // Dagger inject constructor
        this.theme = theme;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        return Response.builder()
                .setJSONContent(Maps.builder(String.class, Object.class)
                        .put("contributors", Contributors.getContributors())
                        .put("defaultTheme", config.get(DisplaySettings.THEME))
                        .put("gmPieColors", theme.getPieColors(ThemeVal.GRAPH_GM_PIE))
                        .build())
                .build();
    }
}
