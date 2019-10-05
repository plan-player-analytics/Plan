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
package com.djrapitops.plan.commands.subcommands.manage;

import com.djrapitops.plan.gathering.importing.ImportSystem;
import com.djrapitops.plan.gathering.importing.importers.Importer;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Optional;

/**
 * This manage SubCommand is used to import data from 3rd party plugins.
 *
 * @author Rsl1122
 */
@Singleton
public class ManageImportCommand extends CommandNode {

    private final Locale locale;
    private final DBSystem dbSystem;
    private final Processing processing;
    private final ImportSystem importSystem;

    @Inject
    public ManageImportCommand(
            Locale locale,
            DBSystem dbSystem,
            Processing processing,
            ImportSystem importSystem
    ) {
        super("import", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.dbSystem = dbSystem;
        this.processing = processing;
        this.importSystem = importSystem;

        setArguments("<plugin>/list", "[import args]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_IMPORT));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_IMPORT));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, "1+", Arrays.toString(this.getArguments()))));

        String importArg = args[0];

        if ("list".equals(importArg)) {
            sender.sendMessage(locale.getString(ManageLang.IMPORTERS));
            importSystem.getImporterNames().forEach(name -> sender.sendMessage("- " + name));
            return;
        }

        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        findAndProcessImporter(sender, importArg);
    }

    private void findAndProcessImporter(Sender sender, String importArg) {
        Optional<Importer> foundImporter = importSystem.getImporter(importArg);
        if (foundImporter.isPresent()) {
            Importer importer = foundImporter.get();
            processing.submitNonCritical(() -> {
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));
                importer.processImport();
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            });
        } else {
            sender.sendMessage(locale.getString(ManageLang.FAIL_IMPORTER_NOT_FOUND, importArg));
        }
    }
}
