package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.ImporterManager;
import main.java.com.djrapitops.plan.utilities.Condition;

/**
 * This manage subcommand is used to import data from 3rd party plugins.
 * <p>
 * Supported plugins (v3.0.0) : OnTime
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageImportCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageImportCommand(Plan plugin) {
        super("import",
                CommandType.CONSOLE,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_IMPORT).toString(),
                "<plugin>/list [import args]");

        this.plugin = plugin;

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
