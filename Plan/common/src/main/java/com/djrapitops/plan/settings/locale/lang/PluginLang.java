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
package com.djrapitops.plan.settings.locale.lang;

/**
 * {@link Lang} implementation for Language that is logged when the plugin enables or disables.
 *
 * @author AuroraLS3
 */
public enum PluginLang implements Lang {
    ENABLED("plugin.enable.enabled", "Enable", "Player Analytics Enabled."),
    ENABLED_WEB_SERVER("plugin.enable.webserver", "Enable - WebServer", "Webserver running on PORT ${0} ( ${1} )"),
    ENABLED_DATABASE("plugin.enable.database", "Enable - Database", "${0}-database connection established."),
    API_ADD_RESOURCE_JS("plugin.apiJSAdded", "API - js+", "PageExtension: ${0} added javascript(s) to ${1}, ${2}"),
    API_ADD_RESOURCE_CSS("plugin.apiCSSAdded", "API - css+", "PageExtension: ${0} added stylesheet(s) to ${1}, ${2}"),
    RELOAD_LOCALE("plugin.localeReloaded", "API - locale reload", "Custom locale.yml was modified so it was reloaded and is now in use."),

    ENABLE_NOTIFY_PROXY_ADDRESS("plugin.enable.notify.proxyAddress", "Enable - Notify proxy address", "Proxy server detected in the database - Proxy Webserver address is '${0}'."),
    ENABLE_NOTIFY_PROXY_DISABLED_WEBSERVER("plugin.enable.notify.proxyDisabledWebserver", "Enable - Notify proxy disabled webserver", "Disabling Webserver on this server - You can override this behavior by setting '${0}' to false."),
    ENABLE_NOTIFY_SETTING_CHANGE("plugin.enable.notify.settingChange", "Enable - Notify settingChange", "Note: Set '${0}' to ${1}"),
    ENABLE_NOTIFY_STORING_PRESERVED_SESSIONS("plugin.enable.notify.storeSessions", "Enable - Storing preserved sessions", "Storing sessions that were preserved before previous shutdown."),
    ENABLE_NOTIFY_EMPTY_IP("plugin.enable.notify.emptyIP", "Enable - Notify Empty IP", "IP in server.properties is empty & Alternative_IP is not in use. Incorrect links might be given!"),
    ENABLE_NOTIFY_BAD_IP("plugin.enable.notify.badIP", "Enable - Notify Bad IP", "0.0.0.0 is not a valid address, set up Alternative_IP settings. Incorrect links might be given!"),
    ENABLE_NOTIFY_WEB_SERVER_DISABLED("plugin.enable.notify.webserverDisabled", "Enable - Notify Webserver disabled", "WebServer was not initialized. (WebServer.DisableWebServer: true)"),
    ENABLE_NOTIFY_GEOLOCATIONS_INTERNET_REQUIRED("plugin.enable.notify.geoInternetRequired", "Enable - Notify Geolocations Internet Required", "Plan Requires internet access on first run to download GeoLite2 Geolocation database."),
    ENABLE_NOTIFY_GEOLOCATIONS_DISABLED("plugin.enable.notify.geoDisabled", "Enable - Notify Geolocations disabled", "Geolocation gathering is not active. (Data.Geolocations: false)"),
    ENABLE_FAIL_DB("plugin.enable.fail.database", "Enable FAIL - Database", "${0}-Database Connection failed: ${1}"),
    ENABLE_FAIL_WRONG_DB("plugin.enable.fail.databaseType", "Enable FAIL - Wrong Database Type", "${0} is not a supported Database"),
    ENABLE_FAIL_DB_PATCH("plugin.enable.fail.databasePatch", "Enable FAIL - Database Patch", "Database Patching failed, plugin has to be disabled. Please report this issue"),
    ENABLE_FAIL_NO_WEB_SERVER_PROXY("plugin.enable.fail.webServer", "Enable FAIL - WebServer (Proxy)", "WebServer did not initialize!"),
    ENABLE_FAIL_GEODB_WRITE("plugin.enable.fail.geoDBWrite", "Enable FAIL - GeoDB Write", "Something went wrong saving the downloaded GeoLite2 Geolocation database"),

    WEB_SERVER_FAIL_PORT_BIND("plugin.webserver.fail.portInUse", "WebServer FAIL - Port Bind", "WebServer was not initialized successfully. Is the port (${0}) in use?"),
    WEB_SERVER_FAIL_SSL_CONTEXT("plugin.webserver.fail.SSLContext", "WebServer FAIL - SSL Context", "WebServer: SSL Context Initialization Failed."),
    WEB_SERVER_FAIL_STORE_LOAD("plugin.webserver.fail.certStoreLoad", "WebServer FAIL - Store Load", "WebServer: SSL Certificate loading Failed."),
    WEB_SERVER_FAIL_EMPTY_FILE("plugin.webserver.fail.certFileEOF", "WebServer FAIL - EOF", "WebServer: EOF when reading Certificate file. (Check that the file is not empty)"),
    WEB_SERVER_NOTIFY_NO_CERT_FILE("plugin.webserver.notify.noCertFile", "WebServer - Notify no Cert file", "WebServer: Certificate KeyStore File not Found: ${0}"),
    WEB_SERVER_NOTIFY_HTTP("plugin.webserver.notify.http", "WebServer - Notify HTTP", "WebServer: No Certificate -> Using HTTP-server for Visualization."),
    WEB_SERVER_NOTIFY_USING_PROXY_MODE("plugin.webserver.notify.reverseProxy", "WebServer - Notify Using Proxy", "WebServer: Proxy-mode HTTPS enabled, make sure that your reverse-proxy is routing using HTTPS and Plan Alternative_IP.Address points to the Proxy"),
    WEB_SERVER_NOTIFY_HTTP_USER_AUTH("plugin.webserver.notify.authDisabledNoHTTPS", "WebServer - Notify HTTP User Auth", "WebServer: User Authorization Disabled! (Not secure over HTTP)"),
    WEB_SERVER_NOTIFY_HTTPS_USER_AUTH("plugin.webserver.notify.authDisabledConfig", "WebServer - Notify HTTPS User Auth", "WebServer: User Authorization Disabled! (Disabled in config)"),
    WEB_SERVER_NOTIFY_IP_WHITELIST("plugin.webserver.notify.ipWhitelist", "Webserver - Notify IP Whitelist", "Webserver: IP Whitelist is enabled."),
    WEB_SERVER_NOTIFY_IP_WHITELIST_BLOCK("plugin.webserver.notify.ipWhitelistBlock", "Webserver - Notify IP Whitelist Block", "Webserver: ${0} was denied access to '${1}'. (not whitelisted)"),
    WEB_SERVER_NOTIFY_CERT_EXPIRE_DATE("plugin.webserver.notify.certificateExpiresOn", "Webserver notify - Cert expiry", "Webserver: Loaded certificate is valid until ${0}."),
    WEB_SERVER_NOTIFY_CERT_EXPIRE_DATE_SOON("plugin.webserver.notify.certificateExpiresSoon", "Webserver notify - Cert expiry soon", "Webserver: Certificate expires in ${0}, consider renewing the certificate."),
    WEB_SERVER_NOTIFY_CERT_EXPIRE_DATE_PASSED("plugin.webserver.notify.certificateExpiresPassed", "Webserver notify - Cert expiry passed", "Webserver: Certificate has expired, consider renewing the certificate."),
    WEB_SERVER_NOTIFY_CERT_NO_SUCH_ALIAS("plugin.webserver.notify.certificateNoSuchAlias", "Webserver notify - Cert no alias", "Webserver: Certificate with alias '${0}' was not found inside the keystore file '${1}'."),

    DISABLED("plugin.disable.disabled", "Disable", "Player Analytics Disabled."),
    DISABLED_WEB_SERVER("plugin.disable.webserver", "Disable - WebServer", "Webserver has been disabled."),
    DISABLED_PROCESSING("plugin.disable.database", "Disable - Processing", "Processing critical unprocessed tasks. (${0})"),
    DISABLED_PROCESSING_COMPLETE("plugin.disable.processingComplete", "Disable - Processing Complete", "Processing complete."),
    DISABLED_UNSAVED_SESSIONS("plugin.disable.savingSessions", "Disable - Unsaved Session Save", "Saving unfinished sessions.."),
    DISABLED_UNSAVED_SESSIONS_TIMEOUT("plugin.disable.savingSessionsTimeout", "Disable - Unsaved Session Save Timeout", "Timeout hit, storing the unfinished sessions on next enable instead."),
    DISABLED_WAITING_SQLITE("plugin.disable.waitingDb", "Disable - Waiting SQLite", "Waiting queries to finish to avoid SQLite crashing JVM.."),
    DISABLED_WAITING_SQLITE_COMPLETE("plugin.disable.waitingDbComplete", "Disable - Waiting SQLite Complete", "Closed SQLite connection."),
    DISABLED_WAITING_TRANSACTIONS("plugin.disable.waitingTransactions", "Disable - Waiting Transactions", "Waiting for unfinished transactions to avoid data loss.."),
    DISABLED_WAITING_TRANSACTIONS_COMPLETE("plugin.disable.waitingTransactionsComplete", "Disable - Waiting Transactions Complete", "Transaction queue closed."),

    VERSION_NEWEST("plugin.version.isLatest", "Version - Latest", "You're using the latest version."),
    VERSION_AVAILABLE("plugin.version.updateAvailable", "Version - New", "New Release (${0}) is available ${1}"),
    VERSION_AVAILABLE_SPIGOT("plugin.version.updateAvailableSpigot", "Version - New (old)", "New Version is available at ${0}"),
    VERSION_AVAILABLE_DEV("plugin.version.isDev", "Version - DEV", " This is a DEV release."),
    VERSION_FAIL_READ_VERSIONS("plugin.version.checkFailGithub", "Version FAIL - Read versions.txt", "Version information could not be loaded from Github/versions.txt"),
    VERSION_FAIL_READ_OLD("plugin.version.checkFail", "Version FAIL - Read info (old)", "Failed to check newest version number"),

    VERSION_UPDATE("html.version.updateButton", "HTML - Version Update", "Update"),
    VERSION_UPDATE_AVAILABLE("html.version.updateModal.title", "HTML - Version Update Available", "Version ${0} is Available!"),
    VERSION_UPDATE_INFO("html.version.updateModal.text", "HTML - Version Update Info", "A new version has been released and is now available for download."),
    VERSION_UPDATE_DEV("html.version.isDev", "HTML - Version Update Dev", "This version is a DEV release."),
    VERSION_CHANGE_LOG("html.version.changelog", "HTML - Version Change log", "View Changelog"),
    VERSION_DOWNLOAD("html.version.download", "HTML - Version Download", "Download Plan-${0}.jar"),
    VERSION_CURRENT("html.version.current", "HTML - Version Current", "You have version ${0}"),

    DB_APPLY_PATCH("plugin.generic.dbApplyingPatch", "Database - Apply Patch", "Applying Patch: ${0}.."),
    DB_APPLIED_PATCHES("plugin.generic.dbPatchesApplied", "Database - Patches Applied", "All database patches applied successfully."),
    DB_APPLIED_PATCHES_ALREADY("plugin.generic.dbPatchesAlreadyApplied", "Database - Patches Applied Already", "All database patches already applied."),
    DB_NOTIFY_CLEAN("plugin.generic.dbNotifyClean", "Database Notify - Clean", "Removed data of ${0} players."),
    DB_NOTIFY_SQLITE_WAL("plugin.generic.dbNotifySQLiteWAL", "Database Notify - SQLite No WAL", "SQLite WAL mode not supported on this server version, using default. This may or may not affect performance."),
    DB_MYSQL_LAUNCH_OPTIONS_FAIL("plugin.generic.dbFaultyLaunchOptions", "Database MySQL - Launch Options Error", "Launch Options were faulty, using default (${0})"),
    LOADING_SERVER_INFO("plugin.generic.loadingServerInfo", "ServerInfo - Loading", "Loading server identifying information"),
    LOADED_SERVER_INFO("plugin.generic.loadedServerInfo", "ServerInfo - Loaded", "Server identifier loaded: ${0}"),
    DB_SCHEMA_PATCH("plugin.generic.dbSchemaPatch", "Database Notify - Patch", "Database: Making sure schema is up to date.."),
    ;

    private final String key;
    private final String identifier;
    private final String defaultValue;

    PluginLang(String key, String identifier, String defaultValue) {
        this.key = key;
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getKey() {return key;}

    @Override
    public String getDefault() {
        return defaultValue;
    }
}