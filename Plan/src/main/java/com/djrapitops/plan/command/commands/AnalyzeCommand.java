package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This SubCommand is used to run the analysis and access the /server link.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
@Singleton
public class AnalyzeCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final InfoSystem infoSystem;
    private final ServerInfo serverInfo;
    private final WebServer webServer;
    private final Database database;
    private final ConnectionSystem connectionSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public AnalyzeCommand(
            Locale locale,
            Processing processing,
            InfoSystem infoSystem,
            ServerInfo serverInfo,
            WebServer webServer,
            Database database,
            ErrorHandler errorHandler
    ) {
        super("analyze|analyse|analysis|a", Permissions.ANALYZE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.infoSystem = infoSystem;
        connectionSystem = infoSystem.getConnectionSystem();
        this.serverInfo = serverInfo;
        this.webServer = webServer;
        this.database = database;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.ANALYZE));
        setInDepthHelp(locale.getArray(DeepHelpLang.ANALYZE));
        setArguments("[server/id]");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

        processing.submitNonCritical(() -> {
            try {
                Server server = getServer(args).orElseGet(serverInfo::getServer);
                UUID serverUUID = server.getUuid();

                infoSystem.generateAnalysisPage(serverUUID);
                sendWebUserNotificationIfNecessary(sender);
                sendLink(server, sender);
            } catch (DBOpException | WebException e) {
                sender.sendMessage("§cError occurred: " + e.toString());
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        });
    }

    private void sendLink(Server server, ISender sender) {
        String target = "/server/" + server.getName();
        String url = connectionSystem.getMainAddress() + target;
        String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);
        sender.sendMessage(locale.getString(CommandLang.HEADER_ANALYSIS));
        // Link
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(linkPrefix + url);
        } else {
            sender.sendMessage(linkPrefix);
            sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
        }
        sender.sendMessage(">");
    }

    private void sendWebUserNotificationIfNecessary(ISender sender) {
        if (webServer.isAuthRequired() &&
                CommandUtils.isPlayer(sender) &&
                !database.check().doesWebUserExists(sender.getName())) {
            sender.sendMessage("§e" + locale.getString(CommandLang.NO_WEB_USER_NOTIFY));
        }
    }

    private Optional<Server> getServer(String[] args) {
        if (args.length >= 1 && connectionSystem.isServerAvailable()) {
            Map<UUID, Server> bukkitServers = database.fetch().getBukkitServers();
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
