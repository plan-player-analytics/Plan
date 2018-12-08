package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.StringSetting;

/**
 * {@link Setting} values that are in "Database" section.
 *
 * @author Rsl1122
 */
public class ProxySettings {

    public static final Setting<String> IP = new StringSetting("Server.IP");
    public static final Setting<String> NETWORK_NAME = new StringSetting("Network.Name");

    private ProxySettings() {
        /* static variable class */
    }
}