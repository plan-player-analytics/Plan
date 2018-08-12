package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This SubCommand is used to set a server as uninstalled on Plan.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class ManageUninstalledCommand extends CommandNode {

    private final Locale locale;

    public ManageUninstalledCommand(PlanPlugin plugin) {
        super("uninstalled", Permissions.MANAGE.getPermission(), CommandType.ALL_WITH_ARGS);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.MANAGE_UNINSTALLED));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_UNINSTALLED));
        setArguments("[server/id]");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

        Processing.submitNonCritical(() -> {
            try {
                Optional<Server> serverOptional = getServer(args);
                if (!serverOptional.isPresent()) {
                    sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, locale.getString(ManageLang.NO_SERVER)));
                    return;
                }
                Server server = serverOptional.get();
                UUID serverUUID = server.getUuid();
                if (ServerInfo.getServerUUID().equals(serverUUID)) {
                    sender.sendMessage(locale.getString(ManageLang.UNINSTALLING_SAME_SERVER));
                    return;
                }

                Database.getActive().save().setAsUninstalled(serverUUID);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (DBOpException e) {
                sender.sendMessage("§cError occurred: " + e.toString());
                Log.toLog(this.getClass(), e);
            }
        });
    }

    private Optional<Server> getServer(String[] args) {
        if (args.length >= 1 && ConnectionSystem.getInstance().isServerAvailable()) {
            Map<UUID, Server> bukkitServers = Database.getActive().fetch().getBukkitServers();
            String serverIdentifier = getGivenIdentifier(args);
            for (Map.Entry<UUID, Server> entry : bukkitServers.entrySet()) {
                Server server = entry.getValue();

                if (Integer.toString(server.getId()).equals(serverIdentifier)
                        || server.getName().equalsIgnoreCase(serverIdentifier)) {
                    return Optional.of(server);
                }
            }
        }
        return Optional.empty();
    }

    private String getGivenIdentifier(String[] args) {
        StringBuilder idBuilder = new StringBuilder(args[0]);
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                idBuilder.append(" ").append(args[i]);
            }
        }
        return idBuilder.toString();
    }
}
