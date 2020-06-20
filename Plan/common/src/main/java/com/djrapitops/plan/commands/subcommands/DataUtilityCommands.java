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
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.commands.use.CMDSender;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.exceptions.ExportException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Singleton
public class DataUtilityCommands {

    private final Locale locale;
    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final Exporter exporter;
    private final Processing processing;

    @Inject
    public DataUtilityCommands(
            Locale locale,
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            Exporter exporter,
            Processing processing
    ) {
        this.locale = locale;
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.exporter = exporter;
        this.processing = processing;
    }

    private void ensureDatabaseIsOpen() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            throw new IllegalArgumentException(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
        }
    }

    public void onExport(CMDSender sender, Arguments arguments) {
        String exportKind = arguments.get(0)
                .orElseThrow(() -> new IllegalArgumentException("Accepts following as export kind: players, server_json"));

        ensureDatabaseIsOpen();

        getExportFunction(exportKind).accept(sender);
    }

    private Consumer<CMDSender> getExportFunction(String exportArg) {
        if ("players".equals(exportArg)) {
            return this::exportPlayers;
        } else if ("server_json".endsWith(exportArg)) {
            return this::exportServerJSON;
        }
        throw new IllegalArgumentException(locale.getString(ManageLang.FAIL_EXPORTER_NOT_FOUND, exportArg));
    }

    private void exportServerJSON(CMDSender sender) {
        if (config.isFalse(ExportSettings.SERVER_JSON)) {
            throw new IllegalArgumentException("'" + ExportSettings.SERVER_JSON.getPath() + "': false");
        }
        processing.submitNonCritical(() -> {
            try {
                sender.send(locale.getString(ManageLang.PROGRESS_START));
                if (exporter.exportServerJSON(serverInfo.getServer())) {
                    sender.send(locale.getString(ManageLang.PROGRESS_SUCCESS));
                } else {
                    sender.send(locale.get(ManageLang.PROGRESS_FAIL).toString("see '" + ExportSettings.SERVER_JSON.getPath() + "' in config.yml"));
                }
            } catch (ExportException e) {
                sender.send(locale.get(ManageLang.PROGRESS_FAIL).toString(e.getMessage()));
            }
        });
    }

    private void exportPlayers(CMDSender sender) {
        boolean exportPlayerJSON = config.isTrue(ExportSettings.PLAYER_JSON);
        boolean exportPlayerHTML = config.isTrue(ExportSettings.PLAYER_PAGES);
        boolean exportPlayersHtml = config.isTrue(ExportSettings.PLAYERS_PAGE);
        if (!exportPlayerJSON && !exportPlayerHTML) {
            throw new IllegalArgumentException("'" + ExportSettings.PLAYER_JSON.getPath() + "' & '" + ExportSettings.PLAYER_PAGES.getPath() + "': false (config.yml)");
        }

        if (exportPlayersHtml) {
            processing.submitNonCritical(exporter::exportPlayersPage);
        }
        processing.submitNonCritical(() -> performExport(sender, exportPlayerJSON, exportPlayerHTML));
    }

    private void performExport(CMDSender sender, boolean exportPlayerJSON, boolean exportPlayerHTML) {
        sender.send(locale.getString(ManageLang.PROGRESS_START));

        Map<UUID, String> players = dbSystem.getDatabase().query(UserIdentifierQueries.fetchAllPlayerNames());
        int size = players.size();
        int failed = 0;

        int i = 1;
        for (Map.Entry<UUID, String> entry : players.entrySet()) {
            try {
                if (exportPlayerJSON) exporter.exportPlayerJSON(entry.getKey(), entry.getValue());
                if (exportPlayerHTML) exporter.exportPlayerPage(entry.getKey(), entry.getValue());
            } catch (ExportException e) {
                failed++;
            }
            i++;
            if (i % 1000 == 0) {
                sender.send(i + " / " + size + " processed..");
            }
        }
        sender.send(locale.getString(ManageLang.PROGRESS_SUCCESS));
        if (failed != 0) {
            sender.send(locale.getString(ManageLang.PROGRESS_FAIL));
            sender.send(" §2✔: §f" + (i - failed));
            sender.send(" §c✕: §f" + failed);
        }
    }
}
