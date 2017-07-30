package main.java.com.djrapitops.plan;

import org.bukkit.Server;

/**
 * Class responsible for holding server variable values that do not change
 * without a reload.
 *
 * @author Rsl1122
 * @since 3.4.1
 */
public class ServerVariableHolder {

    private final String ip;
    private final boolean usingPaper;

    /**
     * Constructor, grabs the variables.
     *
     * @param server instance the plugin is running on.
     */
    public ServerVariableHolder(Server server) {
        ip = server.getIp();

        String serverName = server.getName();
        usingPaper = serverName.equals("Paper")
                || serverName.equals("TacoSpigot"); //Fork of Paper
    }

    /**
     * Ip string in server.properties.
     *
     * @return the ip.
     */
    public String getIp() {
        return ip;
    }

    /**
     * Returns if the server is using PaperSpigot.
     *
     * @return if the server is using PaperSpigot.
     */
    public boolean isUsingPaper() {
        return usingPaper;
    }
}
