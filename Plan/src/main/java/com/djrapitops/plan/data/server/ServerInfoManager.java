/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.server;


import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.tables.ServerTable;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ServerInfoManager {

    private ServerInfo serverInfo;
    private ServerInfoFile serverInfoFile;
    private ServerTable serverTable;

    public ServerInfoManager(Plan plugin) {
        Database db = plugin.getDB();
        if ("sqlite".equals(db.getConfigName())) {
            return;
        }
        try {
            serverInfoFile = new ServerInfoFile(plugin);
        } catch (IOException | InvalidConfigurationException e) {
            Log.toLog(this.getClass().getName(), e);
            Log.error("Failed to read server info from local file, disabling plugin.");
            plugin.disablePlugin();
        }

        serverTable = db.getServerTable();

        int serverID = serverInfoFile.getID();
        if (serverID == -1) {
            registerServer(plugin);
        }
    }

    private void registerServer(Plan plugin) {
        UUID serverUUID = generateNewUUID(plugin.getServer());
        // TODO Clean Up HtmlUtils so this method can make sense
        String[] address = (HtmlUtils.getProtocol() + "/" + HtmlUtils.getIP()).split(":");
        String webAddress = address[0];
        int port = Integer.parseInt(address[1]);
        String name = Settings.SERVER_NAME.toString();
        serverTable.saveCurrentServerInfo(new ServerInfo(-1, serverUUID, name, webAddress, port));
    }

    public UUID generateNewUUID(Server server) {
        String seed = server.getName() + server.getIp() + server.getPort() + server.getVersion() + server.getBukkitVersion();
        return UUID.nameUUIDFromBytes(seed.getBytes());
    }


}