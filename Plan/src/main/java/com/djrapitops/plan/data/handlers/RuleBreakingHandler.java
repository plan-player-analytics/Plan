package main.java.com.djrapitops.plan.data.handlers;

import java.util.Date;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Rsl1122
 */
public class RuleBreakingHandler {

    private final DataCacheHandler handler;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     * @param h Current instance of DataCacheHandler
     */
    public RuleBreakingHandler(Plan plugin, DataCacheHandler h) {
        this.handler = h;
    }

    /**
     * Update if player is banned or not on logout.
     *
     * @param isBanned
     * @param data UserData matching Player
     */
    public void handleLogout(boolean isBanned, UserData data) {
        data.updateBanned(isBanned);
    }

    /**
     * Update if player is banned or not on kick.
     *
     * @param data UserData matching Player
     */
    public void handleKick(UserData data) {
        data.setTimesKicked(data.getTimesKicked() + 1);
        data.setPlayTime(data.getPlayTime() + (new Date().getTime() - data.getLastPlayed()));
        data.setLastPlayed(new Date().getTime());
    }
}
