package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.FormattingUtils;
import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.handling.importing.ImportUtils;
import main.java.com.djrapitops.plan.data.handling.importing.Importer;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.Check;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (!Check.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG) + " " + this.getArguments(), sender)) {
            return true;
        }

        String importFromPlugin = args[0].toLowerCase();

        Map<String, Importer> importPlugins = ImportUtils.getImporters();
        if (importFromPlugin.equals("list")) {
            list(importPlugins, sender);
            return true;
        }

        if (!Check.isTrue(importPlugins.keySet().contains(importFromPlugin), Locale.get(Msg.MANAGE_FAIL_INCORRECT_PLUGIN) + importFromPlugin, sender)) {
            return true;
        }

        if (!Check.isTrue(ImportUtils.isPluginEnabled(importFromPlugin), Locale.get(Msg.MANAGE_FAIL_PLUGIN_NOT_ENABLED) + importFromPlugin, sender)) {
            return true;
        }

        String[] importArguments = FormattingUtils.removeFirstArgument(args);

        final Importer importer = importPlugins.get(importFromPlugin);
        runImportTask(sender, importer, importArguments);
        return true;
    }

    private void runImportTask(ISender sender, final Importer importer, String... importArguments) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("ImportTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_IMPORT).toString());
                    List<UUID> uuids = Fetch.getIOfflinePlayers().stream().map(IOfflinePlayer::getUniqueId).collect(Collectors.toList());
                    if (importer.importData(uuids, importArguments)) {
                        sender.sendMessage(Locale.get(Msg.MANAGE_INFO_SUCCESS).toString());
                    } else {
                        sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                    }
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    private void list(Map<String, Importer> importers, ISender sender) {
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());
        importers.forEach((string, importer) -> sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_LIST_BALL) + " " + string + ": " + importer.getInfo()));
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());
    }
}
