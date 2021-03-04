/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.settings.config.paths;

import com.djrapitops.plan.settings.config.paths.key.*;

import java.util.List;

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
    public static final Setting<String> CORS_ALLOW_ORIGIN = new StringSetting("Webserver.Security.CORS.Allow_origin");
    public static final Setting<String> CERTIFICATE_PATH = new StringSetting("Webserver.Security.SSL_certificate.KeyStore_path");
    public static final Setting<String> CERTIFICATE_KEYPASS = new StringSetting("Webserver.Security.SSL_certificate.Key_pass");
    public static final Setting<String> CERTIFICATE_STOREPASS = new StringSetting("Webserver.Security.SSL_certificate.Store_pass");
    public static final Setting<String> CERTIFICATE_ALIAS = new StringSetting("Webserver.Security.SSL_certificate.Alias");
    public static final Setting<Boolean> IP_WHITELIST_X_FORWARDED = new BooleanSetting("Webserver.Security.Use_X-Forwarded-For_Header");
    public static final Setting<Boolean> IP_WHITELIST = new BooleanSetting("Webserver.Security.IP_whitelist");
    public static final Setting<List<String>> WHITELIST = new StringListSetting("Webserver.Security.IP_whitelist.Whitelist");
    public static final Setting<Boolean> DISABLED = new BooleanSetting("Webserver.Disable_Webserver");
    public static final Setting<Boolean> DISABLED_AUTHENTICATION = new BooleanSetting("Webserver.Security.Disable_authentication");
    public static final Setting<String> EXTERNAL_LINK = new StringSetting("Webserver.External_Webserver_address");

    private WebserverSettings() {
        /* static variable class */
    }
}