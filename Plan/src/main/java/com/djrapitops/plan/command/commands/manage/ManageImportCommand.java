package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.RslRunnable;
import com.djrapitops.javaplugin.utilities.FormattingUtils;
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
import main.java.com.djrapitops.plan.utilities.Check;
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
        if (!Check.ifTrue(args.length >= 1, Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE + " " + Phrase.USE_IMPORT, sender)) {
            return true;
        }

        String importFromPlugin = args[0].toLowerCase();

        Map<String, Importer> importPlugins = ImportUtils.getImporters();
        if (importFromPlugin.equals("list")) {
            list(importPlugins, sender);
            return true;
        }

        if (!Check.ifTrue(importPlugins.keySet().contains(importFromPlugin), Phrase.MANAGE_ERROR_INCORRECT_PLUGIN + importFromPlugin, sender)) {
            return true;
        }
        if (!Check.ifTrue(ImportUtils.isPluginEnabled(importFromPlugin), Phrase.MANAGE_ERROR_PLUGIN_NOT_ENABLED + importFromPlugin, sender)) {
            return true;
        }

        String[] importArguments = FormattingUtils.removeFirstArgument(args);
        
        final Importer importer = importPlugins.get(importFromPlugin);
        runImportTask(sender, importer, importArguments);
        return true;
    }

    private void runImportTask(ISender sender, final Importer importer, String... importArguments) {
        plugin.getRunnableFactory().createNew(new RslRunnable("ImportTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Phrase.MANAGE_IMPORTING + "");
                    List<UUID> uuids = Arrays.stream(getOfflinePlayers()).map(p -> p.getUniqueId()).collect(Collectors.toList());
                    if (importer.importData(uuids, importArguments)) {
                        sender.sendMessage(Phrase.MANAGE_SUCCESS + "");
                    } else {
                        sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                    }
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }

    private void list(Map<String, Importer> importers, ISender sender) {
        sender.sendMessage(Phrase.CMD_FOOTER.parse());
        importers.entrySet().stream().forEach(e -> {
            sender.sendMessage(Phrase.CMD_BALL + " " + e.getKey() + ": " + e.getValue().getInfo());
        });
        sender.sendMessage(Phrase.CMD_FOOTER.parse());
    }
}
