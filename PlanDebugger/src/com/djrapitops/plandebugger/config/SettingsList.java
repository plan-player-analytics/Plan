package com.djrapitops.plandebugger.config;

/**
 *
 * @author Rsl1122
 */
public enum SettingsList {
    DEFAULT(new String[]{"default", "true", "false", "10", "true", "-1", "5", "2", "1", "5", "true", "8804", "false", "your.ip.here:%port%", "true", "bAkEd", "true", "false"});

    private String[] values;

    private SettingsList(String[] values) {
        this.values = values;
    }

    public String[] getValues() {
        return values;
    }
}
