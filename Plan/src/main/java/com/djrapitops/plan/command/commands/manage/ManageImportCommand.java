package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.processing.importing.ImporterManager;
import com.djrapitops.plan.system.processing.importing.importers.Importer;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

/**
 * This manage SubCommand is used to import data from 3rd party plugins.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageImportCommand extends SubCommand {

    public ManageImportCommand() {
        super("import",
                CommandType.CONSOLE,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_IMPORT).toString(),
                "<plugin>/list [import args]");
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE_IMPORT).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG) + " " + this.getArguments(), sender)) {
            return true;
        }

        String importArg = args[0];

        if (importArg.equals("list")) {
            sender.sendMessage("Importers: ");
            ImporterManager.getImporters().stream()
                    .map(Importer::getNames)
                    .map(list -> list.get(0))
                    .forEach(name -> sender.sendMessage("- " + name));
            return true;
        }

        Importer importer = ImporterManager.getImporter(importArg);
        if (importer == null) {
            sender.sendMessage("§eImporter '" + importArg + "' doesn't exist");
            return true;
        }

        RunnableFactory.createNew("Import:" + importArg, new AbsRunnable() {
            @Override
            public void run() {
                try {
                    importer.processImport();
                } finally {
                    cancel();
                }
            }
        }).runTaskAsynchronously();
        return true;
    }
}
