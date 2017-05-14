package main.java.com.djrapitops.plan.data.handling.info;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.GamemodeHandling;
import org.bukkit.GameMode;

/**
 * HandlingInfo Class for GamemodeChangeEvent information.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class GamemodeInfo extends HandlingInfo {

    private GameMode currentGamemode;

    /**
     * Constructor.
     *
     * @param uuid UUID of the player.
     * @param time Epoch ms of the event.
     * @param gm Gamemode the player changed to.
     */
    public GamemodeInfo(UUID uuid, long time, GameMode gm) {
        super(uuid, InfoType.GM, time);
        currentGamemode = gm;
    }

    @Override
    public boolean process(UserData uData) {
        if (currentGamemode == null) {
            return false;
        }
        if (!uData.getUuid().equals(uuid)) {
            return false;
        }
        GamemodeHandling.processGamemodeInfo(uData, time, currentGamemode);
        return true;
    }
}
