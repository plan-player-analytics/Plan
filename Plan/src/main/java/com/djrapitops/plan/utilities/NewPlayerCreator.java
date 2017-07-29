package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.player.Gamemode;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.data.UserData;

/**
 * @author Rsl1122
 */
public class NewPlayerCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private NewPlayerCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a new instance of UserData with default values.
     *
     * @param player Player the UserData is created for.
     * @return a new UserData object
     */
    public static UserData createNewPlayer(IPlayer player) {
        return createNewPlayer(player, player.getGamemode());
    }

    /**
     * Creates a new instance of UserData with default values.
     *
     * @param player OfflinePlayer the UserData is created for.
     * @return a new UserData object
     */
    public static UserData createNewOfflinePlayer(IOfflinePlayer player) {
        return createNewPlayer(player, Gamemode.SURVIVAL);
    }

    /**
     * Creates a new instance of UserData with default values.
     *
     * @param player Player the UserData is created for.
     * @param gm     Gamemode set as the starting Gamemode
     * @return a new UserData object
     */
    public static UserData createNewPlayer(IOfflinePlayer player, Gamemode gm) {
        long registered = player.getFirstPlayed();
        UserData data = new UserData(player.getUniqueId(), registered, player.isOp(), "SURVIVAL", player.getName(), player.isOnline());
        data.setLastGamemode(gm.name());
        data.setLastPlayed(MiscUtils.getTime());
        data.setPlayTime(0L);
        data.setTimesKicked(0);
        data.setLoginTimes(0);
        data.setLastGmSwapTime(0L);
        data.setDeaths(0);
        data.setMobKills(0);
        data.setBanned(false);
        data.setGeolocation("Not Known");
        return data;
    }

}
