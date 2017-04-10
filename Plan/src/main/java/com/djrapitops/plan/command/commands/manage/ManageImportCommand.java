package main.java.com.djrapitops.plan.command.commands.manage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.importing.Importer;
import main.java.com.djrapitops.plan.data.importing.OnTimeImporter;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author Rsl1122
 */
public class ManageImportCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageImportCommand(Plan plugin) {
        super("import", "plan.manage", Phrase.CMD_USG_MANAGE_IMPORT + "", CommandType.CONSOLE, Phrase.ARG_IMPORT + "");
        this.plugin = plugin;
    }

    /**
     * Subcommand inspect.
     *
     * Adds player's data from DataCache/DB to the InspectCache for amount of
     * time specified in the config, and clears the data from Cache with a timer
     * task.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args Player's name or nothing - if empty sender's name is used.
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        if (args.length < 1) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString() + " " + Phrase.USE_IMPORT);
            return true;
        }

        String importFromPlugin = args[0].toLowerCase();
        List<String> supportedImports = Arrays.asList(new String[]{"ontime"});
        if (!supportedImports.contains(importFromPlugin)) {
            sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_PLUGIN + importFromPlugin);
            return true;
        }
        HashMap<String, Importer> importPlugins = new HashMap<>();
        importPlugins.put("ontime", new OnTimeImporter(plugin));

        if (!importPlugins.get(importFromPlugin).isEnabled()) {
            sender.sendMessage(Phrase.MANAGE_ERROR_PLUGIN_NOT_ENABLED + importFromPlugin);
            return true;
        }

        if (!Arrays.asList(args).contains("-a")) {
            sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_OVERWRITE_SOME.parse(plugin.getDB().getConfigName())));
            return true;
        }

        // Header
        sender.sendMessage(Phrase.MANAGE_IMPORTING + "");
        Set<UUID> uuids = new HashSet<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            uuids.add(p.getUniqueId());
        }
        HashMap<UUID, Long> numbericData = importPlugins.get(importFromPlugin).grabNumericData(uuids);
        BukkitTask asyncImportTask = (new BukkitRunnable() {
            @Override
            public void run() {
                if (importFromPlugin.equals("ontime")) {
                    if (ManageUtils.importOnTime(numbericData, plugin)) {
                        sender.sendMessage(Phrase.MANAGE_SUCCESS + "");
                    }
                }
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);
        return true;
    }
}
