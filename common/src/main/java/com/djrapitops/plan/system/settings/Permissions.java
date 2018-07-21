package com.djrapitops.plan.system.settings;

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
    INSPECT_OTHER("plan.inspect.other"),
    QUICK_INSPECT_OTHER("plan.qinspect.other"),

    ANALYZE("plan.analyze"),
    QUICK_ANALYZE("plan.qanalyze"),

    SEARCH("plan.search"),

    RELOAD("plan.reload"),
    INFO("plan.info"),
    MANAGE("plan.manage"),
    MANAGE_WEB("plan.webmanage"),

    IGNORE_COMMAND_USE("plan.ignore.commanduse"),
    IGNORE_AFK("plan.ignore.afk");

    private final String permission;

    Permissions(String permission) {
        this.permission = permission;
    }

    /**
     * Returns the permission node in plugin.yml.
     *
     * @return permission node eg. plan.inspect
     */
    public String getPermission() {
        return permission;
    }

    /**
     * Same as {@link #getPermission()}.
     *
     * @return permission node eg. plan.inspect
     */
    public String getPerm() {
        return getPermission();
    }
}
