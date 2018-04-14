package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.command.commands.manage.ManageConDebugCommand;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.operation.FetchOperations;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.CheckConnectionRequest;
import com.djrapitops.plan.system.info.request.UpdateCancelRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.update.VersionInfo;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Command that updates all servers in the network
 *
 * @author Rsl1122
 */
public class UpdateCommand extends CommandNode {

    public UpdateCommand() {
        super("update", Permissions.MANAGE.getPermission(), CommandType.ALL);
        setArguments("[-update]/[cancel]");
        setShortHelp("Get change log link or update plugin.");
        setInDepthHelp(
                "/plan update",
                "  Used to update the plugin on the next shutdown\n",
                "  /plan update - get change log link",
                "  /plan update -update - Schedule update to happen on all network servers that are online next time they reboot.",
                "  /plan update cancel - Cancel scheduled update on servers that haven't rebooted yet."
        );
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        if (!VersionCheckSystem.isNewVersionAvailable()) {
            sender.sendMessage("§aYou're running the latest version of Plan.");
            return;
        }

        VersionInfo available = VersionCheckSystem.getInstance().getNewVersionAvailable();
        String downloadUrl = available.getDownloadUrl();

        if (!available.isTrusted()) {
            sender.sendMessage("§cVersion download url did not start with " +
                    "https://github.com/Rsl1122/Plan-PlayerAnalytics/releases/ " +
                    "and might not be trusted. You can download this version manually here (Direct download):");
            sender.sendLink(downloadUrl, downloadUrl);
            return;
        }

        if (args.length == 0) {
            sender.sendLink("Change Log v" + available.getVersion().toString() + ": ", "Click me", available.getChangeLogUrl());
            return;
        }

        String firstArgument = args[0];
        if ("-update".equals(firstArgument)) {
            handleUpdate(sender, args);
        } else if ("cancel".equals(firstArgument)) {
            cancel(sender);
        } else {
            throw new IllegalArgumentException("Unknown argument, use '-update' or 'cancel'");
        }
    }

    private void cancel(ISender sender) {
        try {
            cancel(sender, Database.getActive().fetch().getServers());
            sender.sendMessage("§aUpdate has been cancelled.");
        } catch (DBException e) {
            sender.sendMessage("§cDatabase error occurred, cancel could not be performed.");
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void handleUpdate(ISender sender, String[] args) {
        sender.sendMessage("§aYou can cancel the update on servers that haven't rebooted yet with /plan update cancel.");
        sender.sendMessage("Checking that all servers are online..");
        if (!checkNetworkStatus(sender)) {
            sender.sendMessage("§cNot all servers were online or accessible, you can still update available servers using -force as a 2nd argument.");
            if (args.length <= 1 || !"-force".equals(args[1])) {
                return;
            }
        }
        try {
            List<Server> servers = Database.getActive().fetch().getServers();
            update(sender, servers);
        } catch (DBException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void update(ISender sender, List<Server> servers) {
        for (Server server : servers) {
            if (update(sender, server)) {
                sender.sendMessage("§a" + server.getName() + " scheduled for update.");
            } else {
                sender.sendMessage("§cUpdate failed on a server, cancelling update on all servers..");
                cancel(sender, servers);
                sender.sendMessage("§cUpdate cancelled.");
                break;
            }
        }
    }

    private void cancel(ISender sender, List<Server> servers) {
        for (Server server : servers) {
            cancel(sender, server);
        }

    }

    private void cancel(ISender sender, Server server) {
        try {
            InfoSystem.getInstance().getConnectionSystem().sendInfoRequest(new UpdateCancelRequest(), server);
        } catch (ForbiddenException | GatewayException | InternalErrorException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": Odd Exception: " + e.getClass().getSimpleName());
        } catch (UnauthorizedServerException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": Unauthorized. " + server.getName() + " might be using different database.");
        } catch (ConnectionFailException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
            String address = server.getWebAddress();
            boolean local = address.contains("localhost")
                    || address.startsWith("https://:") // IP empty = Localhost
                    || address.startsWith("http://:") // IP empty = Localhost
                    || address.contains("127.0.0.1");
            if (!local) {
                sender.sendMessage("§cNon-local address, check that port is open");
            }
        } catch (NotFoundException e) {
            /* Ignored, older version */
        } catch (WebException e) {
            sender.sendMessage("§cCancel failed on " + server.getName() + ": Odd Exception:" + e.getClass().getSimpleName());
        }
    }

    private boolean update(ISender sender, Server server) {
        try {
            InfoSystem.getInstance().getConnectionSystem().sendInfoRequest(new CheckConnectionRequest(), server);
            return true;
        } catch (BadRequestException e) {
            sender.sendMessage("§c" + server.getName() + " has Allow-Update set to false, aborting update.");
            return false;
        } catch (ForbiddenException | GatewayException | InternalErrorException e) {
            sender.sendMessage("§c" + server.getName() + ": Odd Exception: " + e.getClass().getSimpleName());
            return false;
        } catch (UnauthorizedServerException e) {
            sender.sendMessage("§cFail reason: Unauthorized. " + server.getName() + " might be using different database.");
            return false;
        } catch (ConnectionFailException e) {
            sender.sendMessage("§cFail reason: " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
            String address = server.getWebAddress();
            boolean local = address.contains("localhost")
                    || address.startsWith("https://:") // IP empty = Localhost
                    || address.startsWith("http://:") // IP empty = Localhost
                    || address.contains("127.0.0.1");
            if (!local) {
                sender.sendMessage("§cNon-local address, check that port is open");
            }
            return false;
        } catch (NotFoundException e) {
            sender.sendMessage("§e" + server.getName() + " is using older version and can not be scheduled for update. " +
                    "You can update it manually, update will proceed.");
            return true;
        } catch (WebException e) {
            sender.sendMessage("§eOdd Exception: " + e.getClass().getSimpleName());
            return false;
        }
    }

    private boolean checkNetworkStatus(ISender sender) {
        try {
            FetchOperations fetch = Database.getActive().fetch();
            Optional<Server> bungeeInformation = fetch.getBungeeInformation();
            if (!bungeeInformation.isPresent()) {
                return true;
            }
            Map<UUID, Server> bukkitServers = fetch.getBukkitServers();
            String accessAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
            boolean success = true;
            for (Server server : bukkitServers.values()) {
                if (!ManageConDebugCommand.testServer(sender, accessAddress, server)) {
                    success = false;
                }
            }
            Server bungee = bungeeInformation.get();
            if (!ManageConDebugCommand.testServer(sender, accessAddress, bungee)) {
                success = false;
            }
            return success;
        } catch (DBException e) {
            sender.sendMessage("§cDatabase error occurred, update has been cancelled.");
            Log.toLog(this.getClass().getName(), e);
            return false;
        }
    }
}