package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This SubCommand is used to run the analysis and access the /server link.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class AnalyzeCommand extends CommandNode {

    public AnalyzeCommand() {
        super("analyze|analyse|analysis|a", Permissions.ANALYZE.getPermission(), CommandType.CONSOLE);
        setShortHelp(Locale.get(Msg.CMD_USG_ANALYZE).parse());
        setInDepthHelp(Locale.get(Msg.CMD_HELP_ANALYZE).toArray());
        setArguments("[server/id]");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        sender.sendMessage(Locale.get(Msg.CMD_INFO_FETCH_DATA).toString());

        Processing.submitNonCritical(() -> {
            try {
                Server server = getServer(args).orElseGet(ServerInfo::getServer);
                UUID serverUUID = server.getUuid();


                InfoSystem.getInstance().generateAnalysisPage(serverUUID);
                sendWebUserNotificationIfNecessary(sender);
                sendLink(server, sender);
            } catch (DBOpException | WebException e) {
                sender.sendMessage("§cError occurred: " + e.toString());
                Log.toLog(this.getClass(), e);
            }
        });
    }

    private void sendLink(Server server, ISender sender) {
        String target = "/server/" + server.getName();
        String url = ConnectionSystem.getAddress() + target;
        String message = Locale.get(Msg.CMD_INFO_LINK).toString();
        sender.sendMessage(Locale.get(Msg.CMD_HEADER_ANALYZE).toString());
        // Link
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(message + url);
        } else {
            sender.sendMessage(message);
            sender.sendLink("   ", Locale.get(Msg.CMD_INFO_CLICK_ME).toString(), url);
        }
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
    }

    private void sendWebUserNotificationIfNecessary(ISender sender) {
        if (WebServerSystem.getInstance().getWebServer().isAuthRequired() && CommandUtils.isPlayer(sender)) {

            boolean senderHasWebUser = Database.getActive().check().doesWebUserExists(sender.getName());
            if (!senderHasWebUser) {
                sender.sendMessage("§e[Plan] You might not have a web user, use /plan register <password>");
            }
        }
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
