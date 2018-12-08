package com.djrapitops.plan.system.settings.paths;

import com.djrapitops.plan.system.settings.paths.key.BooleanSetting;
import com.djrapitops.plan.system.settings.paths.key.IntegerSetting;
import com.djrapitops.plan.system.settings.paths.key.Setting;
import com.djrapitops.plan.system.settings.paths.key.StringSetting;

/**
 * {@link Setting} values that are in "Webserver" section.
 *
 * @author Rsl1122
 */
public class WebserverSettings {

    public static final Setting<Integer> PORT = new IntegerSetting("Webserver.Port");
    public static final Setting<Boolean> SHOW_ALTERNATIVE_IP = new BooleanSetting("Webserver.Alternative_IP");
    public static final Setting<String> ALTERNATIVE_IP = new StringSetting("Webserver.Alternative_IP.Address");
    public static final Setting<String> INTERNAL_IP = new StringSetting("Webserver.Internal_IP");
    public static final Setting<String> CERTIFICATE_PATH = new StringSetting("Webserver.Security.SSL_certificate.KeyStore_path");
    public static final Setting<String> CERTIFICATE_KEYPASS = new StringSetting("Webserver.Security.SSL_certificate.Key_pass");
    public static final Setting<String> CERTIFICATE_STOREPASS = new StringSetting("Webserver.Security.SSL_certificate.Store_pass");
    public static final Setting<String> CERTIFICATE_ALIAS = new StringSetting("Webserver.Security.SSL_certificate.Alias");
    public static final Setting<Boolean> DISABLED = new BooleanSetting("Webserver.Disable_Webserver");
    public static final Setting<String> EXTERNAL_LINK = new StringSetting("Webserver.External_Webserver_address");

    private WebserverSettings() {
        /* static variable class */
    }
}