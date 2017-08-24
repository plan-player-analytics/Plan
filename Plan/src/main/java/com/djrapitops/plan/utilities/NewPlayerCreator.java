package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.player.Gamemode;
import com.djrapitops.plugin.utilities.player.IOfflinePlayer;
import com.djrapitops.plugin.utilities.player.IPlayer;
import main.java.com.djrapitops.plan.data.UserInfo;

/**
 * @author Rsl1122
 * @deprecated Will be removed once it's sure that it's unnecessary
 */
@Deprecated // TODO Remove once sure that this is unnecessary.
public class NewPlayerCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private NewPlayerCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a new instance of UserInfo with default values.
     *
     * @param player Player the UserInfo is created for.
     * @return a new UserInfo object
     */
    public static UserInfo createNewPlayer(IPlayer player) {
        return createNewPlayer(player, player.getGamemode());
    }

    /**
     * Creates a new instance of UserInfo with default values.
     *
     * @param player OfflinePlayer the UserInfo is created for.
     * @return a new UserInfo object
     */
    public static UserInfo createNewOfflinePlayer(IOfflinePlayer player) {
        return createNewPlayer(player, Gamemode.SURVIVAL);
    }

    /**
     * Creates a new instance of UserInfo with default values.
     *
     * @param player Player the UserInfo is created for.
     * @param gm     Gamemode set as the starting Gamemode
     * @return a new UserInfo object
     */
    public static UserInfo createNewPlayer(IOfflinePlayer player, Gamemode gm) {
        return null;
    }

}
