package com.djrapitops.plan.system.settings.config;

/**
 * Represents a path to a config value.
 *
 * @author Rsl1122
 */
public interface Setting {

    /**
     * Used to get the String path of a the config setting.
     * <p>
     * Path separates nested levels with a dot.
     *
     * @return Example: Settings.WebServer.Enabled
     */
    String getPath();

}
