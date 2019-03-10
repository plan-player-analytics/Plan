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
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.system.export.HtmlExport;
import com.djrapitops.plan.system.export.JSONExport;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.command.Sender;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Factory for creating Runnables related to {@link InfoSystem} to run with {@link com.djrapitops.plan.system.processing.Processing}.
 *
 * @author Rsl1122
 */
@Singleton
public class InfoProcessors {

    private final Lazy<PlanConfig> config;
    private final Lazy<HtmlExport> htmlExport;
    private final Lazy<JSONExport> jsonExport;
    private final Lazy<InfoSystem> infoSystem;
    private final Lazy<WebExceptionLogger> webExceptionLogger;

    @Inject
    public InfoProcessors(
            Lazy<PlanConfig> config,
            Lazy<HtmlExport> htmlExport,
            Lazy<JSONExport> jsonExport,
            Lazy<InfoSystem> infoSystem,
            Lazy<WebExceptionLogger> webExceptionLogger
    ) {
        this.config = config;
        this.htmlExport = htmlExport;
        this.jsonExport = jsonExport;
        this.infoSystem = infoSystem;
        this.webExceptionLogger = webExceptionLogger;
    }

    public InspectCacheRequestProcessor inspectCacheRequestProcessor(
            UUID uuid,
            Sender sender,
            String playerName,
            BiConsumer<Sender, String> msgSender
    ) {
        return new InspectCacheRequestProcessor(uuid, sender, playerName, msgSender,
                infoSystem.get(), webExceptionLogger.get()
        );
    }

    public PlayerPageUpdateProcessor playerPageUpdateProcessor(UUID uuid) {
        return new PlayerPageUpdateProcessor(uuid, config.get(), htmlExport.get(), jsonExport.get());
    }
}