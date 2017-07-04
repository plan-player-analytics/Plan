package main.java.com.djrapitops.plan;

/**
 * Permissions class is used easily check every permission node.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public enum Permissions {

    HELP("plan.?"),
    //
    INSPECT("plan.inspect"),
    QUICK_INSPECT("plan.qinspect"),
    INSPECT_OTHER("plan.inspect.other"),
    QUICK_INSPECT_OTHER("plan.qinspect.other"),
    //
    ANALYZE("plan.analyze"),
    QUICK_ANALYZE("plan.qanalyze"),
    //
    SEARCH("plan.search"),
    //
    RELOAD("plan.reload"),
    INFO("plan.info"),
    MANAGE("plan.manage"),
    //
    IGNORE_COMMANDUSE("plan.ignore.commanduse");

    private final String permission;

    private Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Returns the string of the permission node in plugin.yml.
     *
     * @return line of the permission eg. plan.inspect
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Returns the string of the permission node in plugin.yml.
     *
     * Shortcut for getPermission
     *
     * @return line of the permission eg. plan.inspect
     */
    public String getPerm() {
        return getPermission();
    }
}
