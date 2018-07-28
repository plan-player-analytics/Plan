package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.system.locale.lang.Lang;

@Deprecated
public enum Msg implements Lang {

    CMD_FAIL_REQ_ARGS("Cmd FAIL - Requires Arguments"),
    CMD_FAIL_REQ_ONE_ARG("Cmd FAIL - Require only one Argument"),
    CMD_FAIL_NO_PERMISSION("Cmd FAIL - No Permission"),
    CMD_FAIL_USERNAME_NOT_VALID("Cmd FAIL - Invalid Username"),
    CMD_FAIL_USERNAME_NOT_SEEN("Cmd FAIL - Unseen Username"),
    CMD_FAIL_USERNAME_NOT_KNOWN("Cmd FAIL - Unknown Username"),
    CMD_FAIL_TIMEOUT("Cmd FAIL - Timeout"),
    CMD_FAIL_NO_DATA_VIEW("Cmd FAIL - No Data View"),

    CMD_INFO_CLICK_ME("Cmd - Click Me"),
    CMD_INFO_LINK("Cmd - Link"),
    CMD_INFO_RESULTS("Cmd - Results"),
    CMD_INFO_NO_RESULTS("Cmd - No Results"),
    CMD_INFO_RELOAD_COMPLETE("Cmd - Reload Success"),
    CMD_INFO_FETCH_DATA("Cmd - Fetch Data"),
    CMD_INFO_SEARCHING("Cmd - Searching"),

    CMD_HEADER_ANALYZE("Cmd Header - Analysis"),
    CMD_HEADER_INSPECT("Cmd Header - Inspect"),
    CMD_HEADER_INFO("Cmd Header - Info"),
    CMD_HEADER_SEARCH("Cmd Header - Search"),

    CMD_CONSTANT_FOOTER(">Constant - CMD Footer"),
    ;

    private final String identifier;
    private String defaultValue;

    Msg(String identifier) {
        this.identifier = identifier;
        defaultValue = "";
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDefault() {
        return defaultValue;
    }
}
