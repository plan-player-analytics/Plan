package main.java.com.djrapitops.plan.command;

import java.util.UUID;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.utilities.uuid.UUIDUtility;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 * This class contains static methods used by the commands to check whether or
 * not the command should proceed.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class ConditionUtils {

    /**
     * Check if the plugin can display the data.
     *
     * @return true/false
     */
    public static boolean pluginHasViewCapability() {
        final boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        final boolean webserverIsOn = Settings.WEBSERVER_ENABLED.isTrue();
        final boolean usingTextUI = Settings.USE_ALTERNATIVE_UI.isTrue();
        return webserverIsOn || usingAlternativeIP || usingTextUI;
    }

    /**
     * Get the uuid of a playername. Same as UUIDUtility
     *
     * @param playerName name of player.
     * @return UUID
     * @see UUIDUtility
     */
    public static UUID getUUID(String playerName) {
        try {
            return UUIDUtility.getUUIDOf(playerName);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Check if uuid is not null.
     *
     * @param uuid UUID
     * @return is the uuid null?
     */
    public static boolean uuidIsValid(UUID uuid) {
        return uuid != null;
    }

    /**
     * Check if the player has played.
     *
     * @param uuid UUID of player
     * @return has the player played before?
     */
    public static boolean playerHasPlayed(UUID uuid) {
        if (!uuidIsValid(uuid)) {
            return false;
        }
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return p.hasPlayedBefore();
    }
}
