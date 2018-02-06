package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.processing.importing.ImporterManager;
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

        runImport("offlineimporter");
        return true;
    }

    private void runImport(String importer) {
        RunnableFactory.createNew("Import", new AbsRunnable() {
            @Override
            public void run() {
                try {
                    ImporterManager.getImporter(importer).processImport();
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
