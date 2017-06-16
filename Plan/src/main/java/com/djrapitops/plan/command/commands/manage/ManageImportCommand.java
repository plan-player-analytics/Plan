package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
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
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString() + " " + Phrase.USE_IMPORT);
            return true;
        }

        String importFromPlugin = args[0].toLowerCase();
        Map<String, Importer> importPlugins = ImportUtils.getImporters();
        if (!importPlugins.keySet().contains(importFromPlugin)) {
            sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_PLUGIN + importFromPlugin);
            return true;
        }

        if (!ImportUtils.isPluginEnabled(importFromPlugin)) {
            sender.sendMessage(Phrase.MANAGE_ERROR_PLUGIN_NOT_ENABLED + importFromPlugin);
            return true;
        }

        if (!Arrays.asList(args).contains("-a")) {
            sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_OVERWRITE_SOME.parse(plugin.getDB().getConfigName())));
            return true;
        }

        final Importer importer = importPlugins.get(importFromPlugin);
        BukkitTask asyncImportTask = new BukkitRunnable() {
            @Override
            public void run() {
                sender.sendMessage(Phrase.MANAGE_IMPORTING + "");
                List<UUID> uuids = Arrays.stream(getOfflinePlayers()).map(p -> p.getUniqueId()).collect(Collectors.toList());
                if (importer.importData(uuids)) {
                    sender.sendMessage(Phrase.MANAGE_SUCCESS + "");
                } else {
                    sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                }
                this.cancel();
            }
        }.runTaskAsynchronously(plugin);
        return true;
    }
}
