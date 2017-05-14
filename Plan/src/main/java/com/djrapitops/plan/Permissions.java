package main.java.com.djrapitops.plan;

import org.bukkit.command.CommandSender;

/**
 * Permissions class is used easily check every permission node.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public enum Permissions {

    HELP("plan.?"),
    INSPECT("plan.inspect"),
    QUICK_INSPECT("plan.qinspect"),
    QUICK_INSPECT_OTHER("plan.qinspect.other"),
    INSPECT_OTHER("plan.inspect.other"),
    ANALYZE("plan.analyze"),
    QUICK_ANALYZE("plan.qanalyze"),
    SEARCH("plan.search"),
    RELOAD("plan.reload"),
    INFO("plan.info"),
    IGNORE_COMMANDUSE("plan.ignore.commanduse"),
    MANAGE("plan.manage");

    private final String permission;

    private Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Checks if the CommandSender has the permission.
     *
     * @param p entity sending the command (console/player/other)
     * @return CommandSender#hasPermission
     * @see CommandSender
     */
    public boolean userHasThisPermission(CommandSender p) {
        return p.hasPermission(permission);
    }

    /**
     * Returns the string of the permission node in plugin.yml.
     *
     * @return line of the permission eg. plan.inspect
     */
    public String getPermission() {
        return permission;
    }
}
