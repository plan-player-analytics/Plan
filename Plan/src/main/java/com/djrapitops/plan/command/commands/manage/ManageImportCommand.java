package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.importing.ImporterManager;
import com.djrapitops.plan.system.processing.importing.importers.Importer;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Arrays;

/**
 * This manage SubCommand is used to import data from 3rd party plugins.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageImportCommand extends CommandNode {

    private final Locale locale;

    public ManageImportCommand(PlanPlugin plugin) {
        super("import", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setArguments("<plugin>/list", "[import args]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_IMPORT));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_IMPORT));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, "1+", Arrays.toString(this.getArguments()))));

        String importArg = args[0];

        if (importArg.equals("list")) {
            sender.sendMessage(locale.getString(ManageLang.IMPORTERS));
            ImporterManager.getImporters().stream()
                    .map(Importer::getNames)
                    .map(list -> list.get(0))
                    .forEach(name -> sender.sendMessage("- " + name));
            return;
        }

        Importer importer = ImporterManager.getImporter(importArg);
        if (importer == null) {
            sender.sendMessage(locale.getString(ManageLang.FAIL_IMPORTER_NOT_FOUND, importArg));
            return;
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
    }
}
