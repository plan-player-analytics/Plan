package com.djrapitops.plan.system.settings.locale;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Msg {

    ENABLED("Enable"),
    ENABLE_DB_INIT("Enable - Db"),
    ENABLE_DB_INFO("Enable - Db Info"),
    ENABLE_WEBSERVER("Enable - WebServer"),
    ENABLE_WEBSERVER_INFO("Enable - WebServer Info"),
    ENABLE_BOOT_ANALYSIS_INFO("Enable - Boot Analysis 30s Notify"),
    ENABLE_BOOT_ANALYSIS_RUN_INFO("Enable - Boot Analysis Notify"),

    ENABLE_NOTIFY_EMPTY_IP("Enable Notify-Empty IP"),
    ENABLE_NOTIFY_NO_DATA_VIEW("Enable Notify - No data view"),
    ENABLE_NOTIFY_DISABLED_CHATLISTENER("Enable Notify - ChatListener"),
    ENABLE_NOTIFY_DISABLED_COMMANDLISTENER("Enable Notify - Disabled CommandListener"),
    ENABLE_NOTIFY_DISABLED_DEATHLISTENER("Enable Notify - Disabled DeathListener"),

    ENABLE_FAIL_WRONG_DB("Enable FAIL - Wrong Db Type"),
    ENABLE_FAIL_DB("Enable FAIL-Db"),
    ENABLE_DB_FAIL_DISABLE_INFO("Enable Db FAIL - Disable Info"),

    RUN_WARN_QUEUE_SIZE("WARN - Too Small Queue Size"),

    DISABLED("Disable"),
    DISABLE_CACHE_SAVE("Disable - Save"),
    DISABLE_WEBSERVER("Disable - WebServer"),

    ANALYSIS_START("Analysis - Start"),
    ANALYSIS_FETCH_UUID("Analysis - Fetch Phase Start"),
    ANALYSIS_FETCH("Analysis - Fetch Phase"),
    ANALYSIS_PHASE_START("Analysis - Begin Analysis"),
    ANALYSIS_3RD_PARTY("Analysis - Third Party"),
    ANALYSIS_FINISHED("Analysis - Complete"),
    ANALYSIS_FAIL_NO_PLAYERS("Analysis FAIL - No Players"),
    ANALYSIS_FAIL_NO_DATA("Analysis FAIL - No Data"),
    ANALYSIS_FAIL_FETCH_EXCEPTION("Analysis FAIL - Fetch Exception"),

    MANAGE_INFO_CONFIG_REMINDER("Manage - Remind Config Change"),
    MANAGE_INFO_START("Manage - Start"),
    MANAGE_INFO_IMPORT("Manage - Import"),
    MANAGE_INFO_FAIL("Manage - Process Fail"),
    MANAGE_INFO_SUCCESS("Manage - Success"),
    MANAGE_INFO_COPY_SUCCESS("Manage - Copy Success"),
    MANAGE_INFO_MOVE_SUCCESS("Manage - Move Success"),
    MANAGE_INFO_CLEAR_SUCCESS("Manage - Clear Success"),
    MANAGE_INFO_REMOVE_SUCCESS("Manage - Remove Success"),

    MANAGE_FAIL_INCORRECT_PLUGIN("Manage FAIL - Incorrect Plugin"),
    MANAGE_FAIL_PLUGIN_NOT_ENABLED("Manage FAIL - Unenabled Plugin"),
    MANAGE_FAIL_SAME_DB("Manage FAIL - Same DB"),
    MANAGE_FAIL_INCORRECT_DB("Manage FAIL - Incorrect DB"),
    MANAGE_FAIL_FAULTY_DB("Manage FAIL - Faulty DB Connection"),
    MANAGE_FAIL_NO_PLAYERS("Manage FAIL - Empty DB"),
    MANAGE_FAIL_FILE_NOT_FOUND("Manage FAIL - Backup File Not Found"),

    MANAGE_FAIL_CONFIRM("Manage FAIL - Confirm Action"),
    MANAGE_NOTIFY_REWRITE("Manage NOTIFY - Rewrite"),
    MANAGE_NOTIFY_OVERWRITE("Manage NOTIFY - Overwrite"),
    MANAGE_NOTIFY_PARTIAL_OVERWRITE("Manage NOTIFY - Partial Overwrite"),
    MANAGE_NOTIFY_REMOVE("Manage NOTIFY - Remove"),

    CMD_FAIL_REQ_ARGS("Cmd FAIL - Requires Arguments"),
    CMD_FAIL_REQ_ONE_ARG("Cmd FAIL - Require only one Argument"),
    CMD_FAIL_NO_PERMISSION("Cmd FAIL - No Permission"),
    CMD_FAIL_USERNAME_NOT_VALID("Cmd FAIL - Invalid Username"),
    CMD_FAIL_USERNAME_NOT_SEEN("Cmd FAIL - Unseen Username"),
    CMD_FAIL_USERNAME_NOT_KNOWN("Cmd FAIL - Unknown Username"),
    CMD_FAIL_TIMEOUT("Cmd FAIL - Timeout"),
    CMD_FAIL_NO_DATA_VIEW("Cmd FAIL - No Data View"),

    CMD_INFO_ANALYSIS_TEMP_DISABLE("Analysis NOTIFY - Temporary Disable"),
    CMD_INFO_CLICK_ME("Cmd - Click Me"),
    CMD_INFO_LINK("Cmd - Link"),
    CMD_INFO_RESULTS("Cmd - Results"),
    CMD_INFO_NO_RESULTS("Cmd - No Results"),
    CMD_INFO_RELOAD_COMPLETE("Cmd - Reload Success"),
    CMD_INFO_FETCH_DATA("Cmd - Fetch Data"),
    CMD_INFO_SEARCHING("Cmd - Searching"),

    CMD_USG_ANALYZE("Cmd - Usage /plan analyze"),
    CMD_USG_QANALYZE("Cmd - Usage /plan qanalyze"),
    CMD_USG_HELP("Cmd - Usage /plan help"),
    CMD_USG_INFO("Cmd - Usage /plan info"),
    CMD_USG_INSPECT("Cmd - Usage /plan inspect"),
    CMD_USG_QINSPECT("Cmd - Usage /plan qinspect"),
    CMD_USG_LIST("Cmd - Usage /plan list"),
    CMD_USG_MANAGE("Cmd - Usage /plan manage"),
    CMD_USG_MANAGE_BACKUP("Cmd - Usage /plan manage backup"),
    CMD_USG_MANAGE_CLEAN("Cmd - Usage /plan manage clean"),
    CMD_USG_MANAGE_CLEAR("Cmd - Usage /plan manage clear"),
    CMD_USG_MANAGE_DUMP("Cmd - Usage /plan manage dump"),
    CMD_USG_MANAGE_HOTSWAP("Cmd - Usage /plan manage hotswap"),
    CMD_USG_MANAGE_IMPORT("Cmd - Usage /plan manage import"),
    CMD_USG_MANAGE_MOVE("Cmd - Usage /plan manage move"),
    CMD_USG_MANAGE_REMOVE("Cmd - Usage /plan manage remove"),
    CMD_USG_MANAGE_RESTORE("Cmd - Usage /plan manage restore"),
    CMD_USG_RELOAD("Cmd - Usage /plan reload"),
    CMD_USG_SEARCH("Cmd - Usage /plan search"),
    CMD_USG_WEB("Cmd - Usage /plan webuser"),
    CMD_USG_WEB_CHECK("Cmd - Usage /plan webuser check"),
    CMD_USG_WEB_DELETE("Cmd - Usage /plan webuser delete"),
    CMD_USG_WEB_LEVEL("Cmd - Usage /plan webuser level"),
    CMD_USG_WEB_REGISTER("Cmd - Usage /plan webuser register"),

    CMD_HELP_ANALYZE("In Depth Help - /plan analyze ?"),
    CMD_HELP_QANALYZE("In Depth Help - /plan qanalyze ?"),
    CMD_HELP_PLAN("In Depth Help - /plan ?"),
    CMD_HELP_INSPECT("In Depth Help - /plan inspect ?"),
    CMD_HELP_QINSPECT("In Depth Help - /plan qinspect ?"),
    CMD_HELP_LIST("In Depth Help - /plan list ?"),
    CMD_HELP_MANAGE("In Depth Help - /plan manage ?"),
    CMD_HELP_MANAGE_CLEAR("In Depth Help - /plan manage clear ?"),
    CMD_HELP_MANAGE_DUMP("In Depth Help - /plan manage dump ?"),
    CMD_HELP_MANAGE_HOTSWAP("In Depth Help - /plan manage hotswap ?"),
    CMD_HELP_MANAGE_IMPORT("In Depth Help - /plan manage import ?"),
    CMD_HELP_MANAGE_REMOVE("In Depth Help - /plan manage remove ?"),
    CMD_HELP_SEARCH("In Depth Help - /plan search ?"),
    CMD_HELP_WEB("In Depth Help - /plan webuser ?"),
    CMD_HELP_WEB_REGISTER("In Depth Help - /plan webuser register ?"),

    CMD_HEADER_ANALYZE("Cmd Header - Analysis"),
    CMD_HEADER_INSPECT("Cmd Header - Inspect"),
    CMD_HEADER_INFO("Cmd Header - Info"),
    CMD_HEADER_SEARCH("Cmd Header - Search"),

    CMD_CONSTANT_LIST_BALL(">Constant - List Ball"),
    CMD_CONSTANT_FOOTER(">Constant - CMD Footer"),

    HTML_NO_PLUGINS("Html - No Extra Plugins"),
    HTML_BANNED("Html - Banned"),
    HTML_OP("Html - OP"),
    HTML_ONLINE("Html - Online"),
    HTML_OFFLINE("Html - Offline"),
    HTML_ACTIVE("Html - Active"),
    HTML_INACTIVE("Html - Inactive"),
    HTML_TABLE_NO_KILLS("Html - Table No Kills"),;

    private final String identifier;

    Msg(String identifier) {
        this.identifier = identifier;
    }

    public static Map<String, Msg> getIdentifiers() {
        return Arrays.stream(values()).collect(Collectors.toMap(Msg::getIdentifier, Function.identity()));
    }

    public String getIdentifier() {
        return identifier;
    }
}
