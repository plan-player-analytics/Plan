package com.djrapitops.plandebugger.config;

import com.djrapitops.plan.Plan;
import com.djrapitops.plandebugger.PlanDebugger;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Rsl1122
 */
public class ConfigSetter {

    private Plan plan;
    private PlanDebugger debug;
    private String[] originalSettings;

    public ConfigSetter(PlanDebugger debug, Plan plan) {
        this.plan = plan;
        this.debug = debug;
        originalSettings = getDefaultSettings();
    }

    public void setSettings(SettingsList list) {
        setSettings(list.getValues());
        debug.log("SETTINGS Set settings to "+list.name());
    }
    
    public void setSettings(String[] s) {
        FileConfiguration c = plan.getConfig();
        c.set(Settings.LOCALE.getPath(), s[0]);
        c.set(Settings.GATHERLOCATIONS.getPath(), s[1]);
        c.set(Settings.ANALYSIS_LOG_TO_CONSOLE.getPath(), s[2]);
        c.set(Settings.ANALYSIS_MINUTES_FOR_ACTIVE.getPath(), s[3]);
        c.set(Settings.ANALYSIS_REFRESH_ON_ENABLE.getPath(), s[4]);
        c.set(Settings.ANALYSIS_AUTO_REFRESH.getPath(), s[5]);
        c.set(Settings.CLEAR_INSPECT_CACHE.getPath(), s[6]);
        c.set(Settings.SAVE_CACHE_MIN.getPath(), s[7]);
        c.set(Settings.SAVE_SERVER_MIN.getPath(), s[8]);
        c.set(Settings.CLEAR_CACHE_X_SAVES.getPath(), s[9]);
        c.set(Settings.WEBSERVER_ENABLED.getPath(), s[10]);
        c.set(Settings.WEBSERVER_PORT.getPath(), s[11]);
        c.set(Settings.SHOW_ALTERNATIVE_IP.getPath(), s[12]);
        c.set(Settings.ALTERNATIVE_IP.getPath(), s[13]);
        c.set(Settings.SECURITY_IP_UUID.getPath(), s[14]);
        c.set(Settings.SECURITY_CODE.getPath(), s[15]);
        c.set(Settings.PLANLITE_ENABLED.getPath(), s[16]);
        c.set(Settings.USE_ALTERNATIVE_UI.getPath(), s[17]);
        plan.saveConfig();
        plan.reloadConfig();
        plan.onDisable();
        plan.onEnable();
    }

    public void resetSettings() {
        setSettings(originalSettings);
    }

    private String[] getDefaultSettings() {
        FileConfiguration c = plan.getConfig();
        return new String[]{
            c.get(Settings.LOCALE.getPath()) + "",
            c.get(Settings.GATHERLOCATIONS.getPath()) + "",
            c.get(Settings.ANALYSIS_LOG_TO_CONSOLE.getPath()) + "",
            c.get(Settings.ANALYSIS_MINUTES_FOR_ACTIVE.getPath()) + "",
            c.get(Settings.ANALYSIS_REFRESH_ON_ENABLE.getPath()) + "",
            c.get(Settings.ANALYSIS_AUTO_REFRESH.getPath()) + "",
            c.get(Settings.CLEAR_INSPECT_CACHE.getPath()) + "",
            c.get(Settings.SAVE_CACHE_MIN.getPath()) + "",
            c.get(Settings.SAVE_SERVER_MIN.getPath()) + "",
            c.get(Settings.CLEAR_CACHE_X_SAVES.getPath()) + "",
            c.get(Settings.WEBSERVER_ENABLED.getPath()) + "",
            c.get(Settings.WEBSERVER_PORT.getPath()) + "",
            c.get(Settings.SHOW_ALTERNATIVE_IP.getPath()) + "",
            c.get(Settings.ALTERNATIVE_IP.getPath()) + "",
            c.get(Settings.SECURITY_IP_UUID.getPath()) + "",
            c.get(Settings.SECURITY_CODE.getPath()) + "",
            c.get(Settings.PLANLITE_ENABLED.getPath()) + "",
            c.get(Settings.USE_ALTERNATIVE_UI.getPath()) + ""
        };
    }
}
