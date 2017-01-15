package com.djrapitops.planlite;



import org.bukkit.ChatColor;

/**
 *
 * @author Rsl1122
 */
public enum Phrase {
    USERNAME_NOT_VALID(ChatColor.RED + "This Player doesn't exist."),
    USERNAME_NOT_SEEN(ChatColor.RED + "This Player has not played on this server."),
    USERNAME_NOT_KNOWN(ChatColor.RED + "Player not found from the database."),
    COLOR_MAIN(ChatColor.DARK_GREEN),
    COLOR_SEC(ChatColor.GRAY),
    COLOR_TER(ChatColor.WHITE),
    ARROWS_RIGHT("»"),
    BALL("•"),
    ERROR_NO_HOOKS(ChatColor.RED + "[PlanLite] No Hooks enabled - Reload plugin!"),
    COMMAND_SENDER_NOT_PLAYER(ChatColor.RED + "[PlanLite] This command can be only used as a player."),
    COMMAND_REQUIRES_ARGUMENTS(ChatColor.RED + "[PlanLite] Command requires arguments."),
    COMMAND_REQUIRES_ARGUMENTS_ONE(ChatColor.RED + "[PlanLite] Command requires one argument."),
    COMMAND_NO_PERMISSION(ChatColor.RED + "[PlanLite] You do not have the required permmission.");

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
