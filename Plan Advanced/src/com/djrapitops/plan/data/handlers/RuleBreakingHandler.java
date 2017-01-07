package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.UserData;
import java.util.Date;
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
     * Update if player is banned or not.
     *
     * @param event QuitEvent given by Listener
     * @param data UserData matching Player
     */
    public void handleLogout(PlayerQuitEvent event, UserData data) {
        Player player = event.getPlayer();
        data.updateBanned(player);
    }

    /**
     * Update if player is banned or not.
     *
     * @param event KickEvent given by Listener
     * @param data UserData matching Player
     */
    public void handleKick(PlayerKickEvent event, UserData data) {
        Player player = event.getPlayer();
        data.setTimesKicked(data.getTimesKicked() + 1);
        data.setPlayTime(data.getPlayTime() + (new Date().getTime() - data.getLastPlayed()));
        data.setLastPlayed(new Date().getTime());
    }
}
