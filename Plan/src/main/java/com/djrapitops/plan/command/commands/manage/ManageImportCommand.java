package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import com.djrapitops.javaplugin.task.RslTask;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.handling.importing.ImportUtils;
import main.java.com.djrapitops.plan.data.handling.importing.Importer;
import static org.bukkit.Bukkit.getOfflinePlayers;

/**
 * This manage subcommand is used to import data from 3rd party plugins.
 *
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
        super("import", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_IMPORT + "", Phrase.ARG_IMPORT + "");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString() + " " + Phrase.USE_IMPORT);
            return true;
        }

        String importFromPlugin = args[0].toLowerCase();
        if (importFromPlugin.equals("list")) {
            list(sender);
            return true;
        }
        Map<String, Importer> importPlugins = ImportUtils.getImporters();
        if (!importPlugins.keySet().contains(importFromPlugin)) {
            sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_PLUGIN + importFromPlugin);
            return true;
        }

        if (!ImportUtils.isPluginEnabled(importFromPlugin)) {
            sender.sendMessage(Phrase.MANAGE_ERROR_PLUGIN_NOT_ENABLED + importFromPlugin);
            return true;
        }

        String[] arguments = new String[args.length-1];
        for (int i = 1; i < args.length; i++) {
            arguments[i-1] = args[i];
        }
        final Importer importer = importPlugins.get(importFromPlugin);
        RslTask asyncImportTask = new RslBukkitRunnable<Plan>("ImportTask") {
            @Override
            public void run() {
                sender.sendMessage(Phrase.MANAGE_IMPORTING + "");
                List<UUID> uuids = Arrays.stream(getOfflinePlayers()).map(p -> p.getUniqueId()).collect(Collectors.toList());
                if (importer.importData(uuids, arguments)) {
                    sender.sendMessage(Phrase.MANAGE_SUCCESS + "");
                } else {
                    sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                }
                this.cancel();
            }
        }.runTaskAsynchronously();
        return true;
    }

    private void list(ISender sender) {
        sender.sendMessage(Phrase.CMD_FOOTER.parse());
        Map<String, Importer> importers = ImportUtils.getImporters();
        for (String key : importers.keySet()) {
            sender.sendMessage(Phrase.CMD_BALL+" "+key+": "+importers.get(key).getInfo());
        }
        sender.sendMessage(Phrase.CMD_FOOTER.parse());
    }
}
