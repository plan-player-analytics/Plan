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
package com.djrapitops.plan.settings.config.changes;

import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;

/**
 * Class in charge of updating config.yml.
 *
 * @author AuroraLS3
 */
@Singleton
public class ConfigUpdater {

    private final PluginLogger logger;
    private final ErrorLogger errorLogger;

    @Inject
    public ConfigUpdater(
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    public void applyConfigUpdate(Config config) throws IOException {
        ConfigChange[] configEnhancementsPatch = configEnhancementPatch();
        applyChanges(config, configEnhancementsPatch);
        config.save();
    }

    // VisibleForTesting
    ConfigChange[] configEnhancementPatch() {
        return new ConfigChange[]{
                new ConfigChange.Moved("Plugin.Locale", "Plugin.Logging.Locale"),
                new ConfigChange.Moved("Plugin.WriteNewLocaleFileOnEnable", "Plugin.Logging.Create_new_locale_file_on_next_enable"),
                new ConfigChange.Moved("Plugin.Debug", "Plugin.Logging.Debug"),
                new ConfigChange.Moved("Plugin.Dev", "Plugin.Logging.Dev"),
                new ConfigChange.Moved("Plugin.KeepLogsForXDays", "Plugin.Logging.Delete_logs_after_days"),

                new ConfigChange.Moved("Plugin.Check-for-updates", "Plugin.Update_notifications.Check_for_updates"),
                new ConfigChange.Moved("Plugin.Notify-About-DEV-Releases", "Plugin.Update_notifications.Notify_about_DEV_releases"),
                new ConfigChange.Removed("Plugin.Allow-Update-Command"),

                new ConfigChange.Moved("Plugin.Bungee-Override.CopyBungeeConfig", "Plugin.Configuration.Allow_bungeecord_to_manage_settings"),
                new ConfigChange.Removed("Plugin.Bungee-Override"),
                new ConfigChange.Moved("Database.MySQL.LaunchOptions", "Database.MySQL.Launch_options"),

                new ConfigChange.Moved("WebServer.InternalIP", "WebServer.Internal_IP"),
                new ConfigChange.Moved("WebServer.Security.SSL-Certificate.KeyStorePath", "WebServer.Security.SSL-Certificate.KeyStore_path"),
                new ConfigChange.Moved("WebServer.Security.SSL-Certificate.KeyPass", "WebServer.Security.SSL-Certificate.Key_pass"),
                new ConfigChange.Moved("WebServer.Security.SSL-Certificate.StorePass", "WebServer.Security.SSL-Certificate.Store_pass"),
                new ConfigChange.Moved("WebServer.Security.SSL-Certificate", "WebServer.Security.SSL_certificate"),
                new ConfigChange.Moved("WebServer.DisableWebServer", "WebServer.Disable_Webserver"),
                new ConfigChange.Moved("WebServer.ExternalWebServerAddress", "WebServer.External_Webserver_address"),
                new ConfigChange.Moved("WebServer", "Webserver"),
                new ConfigChange.Moved("Commands.AlternativeIP.Enabled", "Webserver.Alternative_IP"),
                new ConfigChange.Moved("Commands.AlternativeIP.Link", "Webserver.Alternative_IP.Address"),

                new ConfigChange.Moved("Data.Geolocations", "Data_gathering.Geolocations"),
                new ConfigChange.Moved("Data.Commands.LogUnknownCommands", "Data_gathering.Commands.Log_unknown"),
                new ConfigChange.Moved("Data.Commands.CombineCommandAliases", "Data_gathering.Commands.Log_aliases_as_main_command"),

                new ConfigChange.Moved("Customization.UseServerTime", "Time.Use_server_timezone"),
                new ConfigChange.Moved("Data.Ping.ServerEnableDelaySeconds", "Time.Delays.Ping_server_enable_delay"),
                new ConfigChange.Moved("Data.Ping.PlayerLoginDelaySeconds", "Time.Delays.Ping_player_join_delay"),
                new ConfigChange.Moved("Data.AFKThresholdMinutes", "Time.Thresholds.AFK_threshold"),
                new ConfigChange.Moved("Analysis.Active.LoginThreshold", "Time.Thresholds.Activity_index.Login_threshold"),
                new ConfigChange.Moved("Analysis.Active.PlaytimeThreshold", "Time.Thresholds.Activity_index.Playtime_threshold"),
                new ConfigChange.Moved("Data.KeepInactivePlayerDataForDays", "Time.Thresholds.Remove_inactive_player_data_after"),
                new ConfigChange.Removed("Analysis.LogProgress"),
                new ConfigChange.Moved("Analysis.AutoRefreshPeriod", "Time.Periodic_tasks.Analysis_refresh_every"),

                new ConfigChange.Moved("Theme.Base", "Display_options.Theme"),
                new ConfigChange.Moved("Customization.Display.SessionsAsTable", "Display_options.Sessions.Replace_accordion_with_table"),
                new ConfigChange.Moved("Customization.Display.LargestWorldPercInSessionTitle", "Display_options.Sessions.Show_most_played_world_in_title"),
                new ConfigChange.Moved("Customization.Display.MaxSessions", "Display_options.Sessions.Show_on_page"),
                new ConfigChange.Moved("Customization.Display.OrderWorldPieByPercentage", "Display_options.Sessions.Order_world_pies_by_percentage"),
                new ConfigChange.Moved("Customization.Display.MaxPlayers", "Display_options.Players_table.Show_on_server_page"),
                new ConfigChange.Moved("Customization.Display.MaxPlayersPlayersPage", "Display_options.Players_table.Show_on_players_page"),
                new ConfigChange.Removed("Customization.Display.PlayerTableFooter"),
                new ConfigChange.Moved("Customization.Display.PlayerIPs", "Display_options.Show_player_IPs"),
                new ConfigChange.Moved("Customization.Display.GapsInGraphData", "Display_options.Graphs.Show_gaps_in_data"),
                new ConfigChange.Moved("Theme.Graphs.TPS.High-Threshold", "Display_options.Graphs.TPS.High_threshold"),
                new ConfigChange.Moved("Theme.Graphs.TPS.Medium-Threshold", "Display_options.Graphs.TPS.Medium_threshold"),
                new ConfigChange.Moved("Theme.Graphs.Disk.High-Threshold", "Display_options.Graphs.Disk_space.High_threshold"),
                new ConfigChange.Moved("Theme.Graphs.Disk.Medium-Threshold", "Display_options.Graphs.Disk_space.Medium_threshold"),
                new ConfigChange.Moved("Commands.Colors", "Display_options.Command_colors"),

                new ConfigChange.Moved("Customization.Formatting.DecimalPoints", "Customization.Formatting.Decimal_points"),
                new ConfigChange.Moved("Customization.Formatting.TimeAmount", "Customization.Formatting.Time_amount"),
                new ConfigChange.Moved("Customization.Formatting.Dates.RecentDays", "Customization.Formatting.Dates.Show_recent_day_names"),
                new ConfigChange.Moved("Customization.Formatting", "Formatting"),

                new ConfigChange.Moved("Customization.WorldAliases", "World_aliases"),
                new ConfigChange.Moved("Analysis.Export.DestinationFolder", "Export.HTML_Export_path"),
                new ConfigChange.Copied("Analysis.Export.Enabled", "Export.Parts.JavaScript_and_CSS"),
                new ConfigChange.Copied("Analysis.Export.Enabled", "Export.Parts.Player_pages"),
                new ConfigChange.Copied("Analysis.Export.Enabled", "Export.Parts.Players_page"),
                new ConfigChange.Moved("Analysis.Export.Enabled", "Export.Parts.Server_page"),

                new ConfigChange.Removed("Commands"),
                new ConfigChange.Removed("Analysis"),
                new ConfigChange.Removed("Data"),
                new ConfigChange.Removed("Customization"),
                new ConfigChange.Removed("Theme"),

                // 5.0.0
                new ConfigChange.Removed("Display_options.Sessions.Replace_accordion_with_table"),
                new ConfigChange.Removed("Display_options.Sessions.Show_most_played_world_in_title"),
                new ConfigChange.Removed("Time.Thresholds.Activity_index.Login_threshold"),
                new ConfigChange.Removed("Time.Periodic_tasks.Clean_caches_every"),
                new ConfigChange.Removed("Time.Periodic_tasks.Analysis_refresh_every"),
                new ConfigChange.Removed("Display_options.Show_player_IPs"),
                new ConfigChange.Removed("Export.Parts.JavaScript_and_CSS"),
                new ConfigChange.Moved("Plugins.LiteBans", "Plugins.Litebans"),
                new ConfigChange.Moved("Plugins.BuyCraft", "Plugins.Buycraft"),
                new ConfigChange.Moved("Plugin.Configuration.Allow_bungeecord_to_manage_settings", "Plugin.Configuration.Allow_proxy_to_manage_settings"),
                new ConfigChange.RemovedComment("Webserver.Disable_Webserver"),
                new ConfigChange.BooleanToString("Time.Use_server_timezone", FormatSettings.TIMEZONE.getPath(), "server", "UTC"),

                new ConfigChange.Removed("Plugin.Logging.Debug"),
                new ConfigChange.Moved("Plugins.PlaceholderAPI.Placeholders", "Plugins.PlaceholderAPI.Tracked_player_placeholders"),

                new ConfigChange.Removed("Database.H2.User"),
                new ConfigChange.Removed("Database.H2.Password"),
                new ConfigChange.Removed("Database.H2"),

                new ConfigChange.MoveLevelDown("World_aliases", "World_aliases.List"),

                new ConfigChange.MovedValue("Webserver.Alternative_IP", "Webserver.Alternative_IP.Enabled"),
                new ConfigChange.MovedValue("Webserver.Security.IP_whitelist", "Webserver.Security.IP_whitelist.Enabled"),
                new ConfigChange.Moved("Formatting.Dates.Show_recent_day_names.DatePattern", "Formatting.Dates.Show_recent_day_names_date_pattern"),
                new ConfigChange.MovedValue("Export.Server_refresh_period", "Export.Server_refresh_period.Time"),
                new ConfigChange.MovedValue("Webserver.Security.Cookies_expire_after", "Webserver.Security.Cookies_expire_after.Time"),
                new ConfigChange.MovedValue("Webserver.Cache.Reduced_refresh_barrier", "Webserver.Cache.Reduced_refresh_barrier.Time"),
                new ConfigChange.MovedValue("Webserver.Cache.Invalidate_disk_cache_after", "Webserver.Cache.Invalidate_disk_cache_after.Time"),
                new ConfigChange.MovedValue("Webserver.Cache.Invalidate_memory_cache_after", "Webserver.Cache.Invalidate_memory_cache_after.Time"),
                new ConfigChange.MovedValue("Webserver.Cache.Invalidate_query_results_on_disk_after", "Webserver.Cache.Invalidate_query_results_on_disk_after.Time"),
                new ConfigChange.MovedValue("Time.Thresholds.Remove_disabled_extension_data_after", "Time.Thresholds.Remove_disabled_extension_data_after.Time"),
                new ConfigChange.MovedValue("Time.Thresholds.Remove_time_series_data_after", "Time.Thresholds.Remove_time_series_data_after.Time"),
                new ConfigChange.MovedValue("Time.Thresholds.Remove_ping_data_after", "Time.Thresholds.Remove_ping_data_after.Time"),
                new ConfigChange.MovedValue("Time.Thresholds.AFK_threshold", "Time.Thresholds.AFK_threshold.Time"),
                new ConfigChange.MovedValue("Time.Thresholds.Remove_inactive_player_data_after", "Time.Thresholds.Remove_inactive_player_data_after.Time"),
                new ConfigChange.MovedValue("Time.Periodic_tasks.Extension_data_refresh_every", "Time.Periodic_tasks.Extension_data_refresh_every.Time"),
                new ConfigChange.MovedValue("Time.Periodic_tasks.Check_DB_for_server_config_files_every", "Time.Periodic_tasks.Check_DB_for_server_config_files_every.Time"),
                new ConfigChange.MovedValue("Time.Periodic_tasks.Clean_Database_every", "Time.Periodic_tasks.Clean_Database_every.Time"),
                new ConfigChange.MovedValue("Time.Delays.Ping_server_enable_delay", "Time.Delays.Ping_server_enable_delay.Time"),
                new ConfigChange.MovedValue("Time.Delays.Ping_player_join_delay", "Time.Delays.Ping_player_join_delay.Time"),
                new ConfigChange.MovedValue("Time.Delays.Wait_for_DB_Transactions_on_disable", "Time.Delays.Wait_for_DB_Transactions_on_disable.Time"),
                new ConfigChange.MovedValue("Time.Thresholds.Activity_index.Playtime_threshold", "Time.Thresholds.Activity_index.Playtime_threshold.Time"),

                new ConfigChange.Removed("Plugin.Frontend_BETA"),
                new ConfigChange.Removed("Plugin.Use_Legacy_Frontend"),
                new ConfigChange.Removed("Customized_files.Enable_web_dev_mode"),
                new ConfigChange.Removed("Customized_files.Plan"),

                new ConfigChange.Moved("Data_gathering.Preserve_join_address_case", "Data_gathering.Join_addresses.Preserve_case"),
                new ConfigChange.Moved("Data_gathering.Preserve_invalid_join_addresses", "Data_gathering.Join_addresses.Preserve_invalid"),
        };
    }

    private void applyChanges(Config config, ConfigChange[] changes) {
        for (ConfigChange change : changes) {
            try {
                if (!change.hasBeenApplied(config)) {
                    change.apply(config);
                    logger.info("Config: " + change.getAppliedMessage());
                }
            } catch (Exception e) {
                errorLogger.error(e, ErrorContext.builder()
                        .whatToDo("Fix write permissions to " + config.getConfigFilePath() + " or Report this")
                        .related("Attempt to change: " + change.getAppliedMessage()).build());
            }
        }
    }
}
