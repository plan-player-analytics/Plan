package com.djrapitops.plan;

import org.bukkit.ChatColor;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public enum Phrase {
    CONFIG_HEADER("Plan Config | More info at https://www.spigotmc.org/wiki/plan-configuration/"),
    DATABASE_TYPE_DOES_NOT_EXIST("That database type doesn't exist."),
    DATABASE_FAILURE_DISABLE("Database initialization has failed, disabling Plan."),
    PLANLITE_REG_HOOK("Registered additional hook, passed on to PlanLite: "),
    USERNAME_NOT_VALID(ChatColor.RED + "[Plan] This Player doesn't exist."),
    USERNAME_NOT_SEEN(ChatColor.RED + "[Plan] This Player has not played on this server."),
    USERNAME_NOT_KNOWN(ChatColor.RED + "[Plan] Player not found from the database."),
    COLOR_MAIN(ChatColor.getByChar(getPlugin(Plan.class).getConfig().getString("Customization.Colors.Commands.Main").charAt(1))),
    COLOR_SEC(ChatColor.getByChar(getPlugin(Plan.class).getConfig().getString("Customization.Colors.Commands.Secondary").charAt(1))),
    COLOR_TER(ChatColor.getByChar(getPlugin(Plan.class).getConfig().getString("Customization.Colors.Commands.Highlight").charAt(1))),
    HCOLOR_ACT_ONL(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityGraph.OnlinePlayers")),
    HCOLOR_ACT_NEW(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityGraph.NewPlayers")),
    HCOLOR_GMP_0(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Survival")),
    HCOLOR_GMP_1(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Creative")),
    HCOLOR_GMP_2(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Adventure")),
    HCOLOR_GMP_3(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.GamemodePie.Spectator")),
    HCOLOR_ACTP_ACT(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.Active")),
    HCOLOR_ACTP_BAN(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.Banned")),
    HCOLOR_ACTP_INA(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.Inactive")),
    HCOLOR_ACTP_JON(getPlugin(Plan.class).getConfig().getString("Customization.Colors.HTML.ActivityPie.JoinedOnce")),
    ARROWS_RIGHT("»"),
    BALL("•"),
    ERROR_PLANLITE("PlanLite not found, if you're have plugins using PlanAPI v1.6.0 download PlanLite."),
    ERROR_NO_DATA_VIEW(ChatColor.YELLOW + "Webserver disabled but Alternative IP/PlanLite not used, no way to view data!"),
    ERROR_WEBSERVER_OFF_ANALYSIS(ChatColor.YELLOW + "[Plan] This command can be only used if the webserver is running on this server."),
    ERROR_WEBSERVER_OFF_INSPECT(ChatColor.YELLOW + "[Plan] This command can be only used if webserver/planlite is enabled on this server."),
    MANAGE_ERROR_INCORRECT_PLUGIN(ChatColor.RED+"[Plan] Plugin not supported: "),
    MANAGE_ERROR_PLUGIN_NOT_ENABLED(ChatColor.RED+"[Plan] Plugin is not enabled: "),
    MANAGE_ERROR_INCORRECT_DB(ChatColor.RED+"[Plan] Incorrect database! (sqlite/mysql accepted): "),
    MANAGE_ERROR_SAME_DB(ChatColor.RED+"[Plan] Can't move to the same database!"),
    MANAGE_DATABASE_FAILURE(ChatColor.RED+"[Plan] One of the databases was not initialized properly."),
    MANAGE_DB_CONFIG_REMINDER(ChatColor.YELLOW+"[Plan] Remember to swap to the new database and reload plugin"),
    MANAGE_ERROR_NO_PLAYERS(ChatColor.RED+"[Plan] Database has no player data!"),
    MANAGE_MOVE_SUCCESS(ChatColor.GREEN+"[Plan] All data moved successfully!"),
    MANAGE_CLEAR_SUCCESS(ChatColor.GREEN+"[Plan] All data cleared successfully!"),
    COMMAND_SENDER_NOT_PLAYER(ChatColor.RED + "[Plan] This command can be only used as a player."),
    COMMAND_REQUIRES_ARGUMENTS(ChatColor.RED + "[Plan] Command requires arguments."),
    COMMAND_ADD_CONFIRMATION_ARGUMENT(ChatColor.RED + "[Plan] Add -a to confirm execution!"),
    COMMAND_REQUIRES_ARGUMENTS_ONE(ChatColor.RED + "[Plan] Command requires one argument."),
    COMMAND_NO_PERMISSION(ChatColor.RED + "[Plan] You do not have the required permmission.");

    private final String text;
    private final ChatColor color;

    private Phrase(final String text) {
        this.text = text;
        this.color = null;
    }

    private Phrase(final ChatColor color) {
        this.color = color;
        this.text = "";
    }

    @Override
    public String toString() {
        return text;
    }

    /**
     * @return Color of the COLOR_ENUM
     */
    public ChatColor color() {
        return color;
    }
}
