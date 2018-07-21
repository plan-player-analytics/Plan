package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.request.CheckConnectionRequest;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.settings.ColorScheme;

import java.util.List;
import java.util.UUID;

/**
 * This manage SubCommand is used to request settings from Bungee so that connection can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageConDebugCommand extends CommandNode {

    public ManageConDebugCommand() {
        super("con", Permissions.MANAGE.getPermission(), CommandType.ALL);
        setShortHelp("Debug Bukkit-Bungee Connections");
    }

    public static boolean testServer(ISender sender, String accessAddress, Server server) {
        String address = server.getWebAddress().toLowerCase();
        boolean usingHttps = address.startsWith("https");
        boolean local = address.contains("localhost")
                || address.startsWith("https://:") // IP empty = Localhost
                || address.startsWith("http://:") // IP empty = Localhost
                || address.contains("127.0.0.1");

        try {

            InfoSystem.getInstance().getConnectionSystem().sendInfoRequest(new CheckConnectionRequest(accessAddress), server);
            sender.sendMessage(getMsgFor(address, usingHttps, local, true, true));
            return true;

        } catch (ForbiddenException | BadRequestException | InternalErrorException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage("§eOdd Exception: " + e.getClass().getSimpleName());
        } catch (UnauthorizedServerException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, true, false));
            sender.sendMessage("§eFail reason: Unauthorized. Server might be using different database.");
        } catch (ConnectionFailException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage("§eFail reason: " + e.getCause().getClass().getSimpleName() + " " + e.getCause().getMessage());
            if (!local) {
                sender.sendMessage("§eNon-local address, check that port is open");
            }
        } catch (GatewayException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, true, false));
        } catch (NotFoundException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage("§eFail reason: Older Plan version on receiving server");
        } catch (WebException e) {
            sender.sendMessage(getMsgFor(address, usingHttps, local, false, false));
            sender.sendMessage("§eOdd Exception: " + e.getClass().getSimpleName());
        }
        return false;
    }

    private static String getMsgFor(String address, boolean usingHttps, boolean local, boolean successTo, boolean successFrom) {
        ColorScheme cs = PlanHelper.getInstance().getColorScheme();
        String tCol = cs.getTertiaryColor();
        String sCol = cs.getSecondaryColor();
        return tCol + address + sCol + ": "
                + (usingHttps ? "HTTPS" : "HTTP") + " : "
                + (local ? "Local" : "External") + " : "
                + "To:" + (successTo ? "§aOK" : "§cFail") + sCol + " : "
                + "From:" + (successFrom ? "§aOK" : "§cFail");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        if (!WebServerSystem.isWebServerEnabled()) {
            sender.sendMessage("§cWebServer is not enabled on this server.");
            return;
        }

        Processing.submitNonCritical(() -> testServers(sender));
    }

    private void testServers(ISender sender) {
        List<Server> servers = Database.getActive().fetch().getServers();

        if (servers.isEmpty()) {
            sender.sendMessage("§cNo Servers found in the database.");
        }

        String accessAddress = WebServerSystem.getInstance().getWebServer().getAccessAddress();
        UUID thisServer = ServerInfo.getServerUUID();
        for (Server server : servers) {
            if (thisServer.equals(server.getUuid())) {
                continue;
            }
            testServer(sender, accessAddress, server);
        }
    }
}
