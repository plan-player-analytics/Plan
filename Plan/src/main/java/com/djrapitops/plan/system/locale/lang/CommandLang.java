package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for general command language.
 *
 * @author Rsl1122
 */
public enum CommandLang implements Lang {
    FAIL_REQ_ARGS("Cmd FAIL - Requires Arguments", "§cArguments required (${0}) ${1}"),
    FAIL_REQ_ONE_ARG("Cmd FAIL - Require only one Argument", "§cSingle Argument required ${1}"),
    FAIL_NO_PERMISSION("Cmd FAIL - No Permission", "§cYou do not have the required permission."),
    FAIL_USERNAME_NOT_VALID("Cmd FAIL - Invalid Username", "§cUser does not have an UUID."),
    FAIL_USERNAME_NOT_KNOWN("Cmd FAIL - Unknown Username", "§cUser has not been seen on this server"),

    FAIL_WEB_USER_EXISTS("Cmd FAIL - WebUser exists", "§cUser already exists!"),
    FAIL_WEB_USER_NOT_EXISTS("Cmd FAIL - WebUser does not exists", "§cUser does not exists!"),
    FAIL_NO_SUCH_FEATURE("Cmd FAIL - No Feature", "§eDefine a feature to disable! (currently supports ${0})"),

    FEATURE_DISABLED("Cmd SUCCESS - Feature disabled", "§aDisabled '${0}' temporarily until next plugin reload."),

    WEB_USER_REGISTER_SUCCESS("Cmd SUCCESS - WebUser register", "§aAdded a new user (${0}) successfully!"),
    WEB_USER_REGISTER_NOTIFY("Cmd Notify - WebUser register", "Registered new user: '${0}' Perm level: ${1}"),
    WEB_USER_LIST("Web User Listing", "  §2${0} §7: §f${1}"),
    NO_WEB_USER_NOTIFY("Cmd Notify - No WebUser", "You might not have a web user, use /plan register <password>"),
    WEB_PERMISSION_LEVELS("Cmd Web - Permission Levels", ">\\§70: Access all pages\\§71: Access '/players' and all player pages\\§72: Access player page with the same username as the webuser\\§73+: No permissions"),

    CONNECT_SUCCESS("Cmd Setup - Success", "§aConnection successful, Plan may restart in a few seconds.."),
    CONNECT_FORBIDDEN("Cmd Setup - Forbidden", "§eConnection succeeded, but Bungee has set-up mode disabled - use '/planbungee setup' to enable it."),
    CONNECT_BAD_REQUEST("Cmd Setup - Bad Request", "§eConnection succeeded, but Receiving server was not a Bungee server. Use Bungee address instead."),
    CONNECT_UNAUTHORIZED("Cmd Setup - Unauthorized", "§eConnection succeeded, but Receiving server didn't authorize this server. Contact Discord for support"),
    CONNECT_FAIL("Cmd Setup - Generic Fail", "§eConnection failed: ${0}"),
    CONNECT_INTERNAL_ERROR("Cmd Setup - Internal Error", "§eConnection succeeded. ${0}, check possible ErrorLog on receiving server's debug page."),
    CONNECT_GATEWAY("Cmd Setup - Gateway Error", "§eConnection succeeded, but Bungee failed to connect to this server (Did current web server restart?). Use /plan m con & /planbungee con to debug."),
    CONNECT_WEBSERVER_NOT_ENABLED("Cmd Setup - WebServer not Enabled", "§cWebServer is not enabled on this server! Make sure it enables on boot!"),
    CONNECT_URL_MISTAKE("Cmd Setup - Url mistake", "§cMake sure you're using the full address (Starts with http:// or https://) - Check Bungee enable log for the full address."),

    LINK_CLICK_ME("Cmd - Click Me", "Click me"),
    LINK_PREFIX("Cmd - Link", "  §2Link: §f"),

    HEADER_SEARCH("Cmd Header - Search", "> §2${0} Results for §f${1}§2:"),
    HEADER_ANALYSIS("Cmd Header - Analysis", "> §2Analysis Results"),
    HEADER_INFO("Cmd Header - Info", "> §2Player Analytics"),
    HEADER_INSPECT("Cmd Header - Inspect", "> §2Player: §f${0}"),
    HEADER_SERVERS("Cmd Header - Servers", "> §2Servers"),
    HEADER_PLAYERS("Cmd Header - Players", "> §2Players"),
    HEADER_WEB_USERS("Cmd Header - Web Users", "> §2${0} Web Users"),
    HEADER_NETWORK("Cmd Header - Network", "> §2Network Page"),

    INFO_VERSION("Cmd Info - Version", "  §2Version: §f${0}"),
    INFO_UPDATE("Cmd Info - Update", "  §2Update Available: §f${0}"),
    INFO_DATABASE("Cmd Info - Database", "  §2Active Database: §f${0}"),
    INFO_BUNGEE_CONNECTION("Cmd Info - Bungee Connection", "  §2Connected to Bungee: §f${0}"),

    QINSPECT_ACTIVITY_INDEX("Cmd Qinspect - Activity Index", "  §2Activity Index: §f${0} | ${1}"),
    QINSPECT_REGISTERED("Cmd Qinspect - Registered", "  §2Registered: §f${0}"),
    QINSPECT_LAST_SEEN("Cmd Qinspect - Last Seen", "  §2Last Seen: §f${0}"),
    QINSPECT_GEOLOCATION("Cmd Qinspect - Geolocation", "  §2Logged in from: §f${0}"),
    QINSPECT_PLAYTIME("Cmd Qinspect - Playtime", "  §2Playtime: §f${0}"),
    QINSPECT_LONGEST_SESSION("Cmd Qinspect - Longest Session", "  §2Longest Session: §f${0}"),
    QINSPECT_TIMES_KICKED("Cmd Qinspect - Times Kicked", "  §2Times Kicked: §f${0}"),
    QINSPECT_PLAYER_KILLS("Cmd Qinspect - Player Kills", "  §2Player Kills: §f${0}"),
    QINSPECT_MOB_KILLS("Cmd Qinspect - Mob Kills", "  §2Mob Kills: §f${0}"),
    QINSPECT_DEATHS("Cmd Qinspect - Deaths", "  §2Deaths: §f${0}"),

    RELOAD_COMPLETE("Cmd Info - Reload Complete", "§aReload Complete"),
    RELOAD_FAILED("Cmd Info - Reload Failed", "§cSomething went wrong during reload of the plugin, a restart is recommended.");

    private final String identifier;
    private final String defaultValue;

    CommandLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}