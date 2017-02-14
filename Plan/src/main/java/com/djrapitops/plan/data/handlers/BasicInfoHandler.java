package main.java.com.djrapitops.plan.data.handlers;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Rsl1122
 */
public class BasicInfoHandler {

    private DataCacheHandler handler;

    /**
     * Class Constructor
     *
     * @param plugin Current instance of Plan
     * @param h Current instance of DataCacheHandler
     */
    public BasicInfoHandler(Plan plugin, DataCacheHandler h) {
        this.handler = h;
    }

    /**
     * Adds new nicknames and IPs to UserData
     *
     * @param event JoinEvent to get the Player
     * @param data UserData matching the Player
     */
    public void handleLogin(PlayerJoinEvent event, UserData data) {
        Player player = event.getPlayer();
        String nickname = player.getDisplayName();
        if (!nickname.isEmpty()) {
            if (data.addNickname(nickname)) {
                data.setLastNick(nickname);
            }
        }
        data.addIpAddress(player.getAddress().getAddress());
    }

    /**
     * Adds new nicknames and IPs to UserData in case of /reload
     *
     * @param player A player that is online when /reload is run
     * @param data UserData matching the Player
     */
    public void handleReload(Player player, UserData data) {
        String nickname = player.getDisplayName();
        if (!nickname.isEmpty()) {
            if (data.addNickname(nickname)) {
                data.setLastNick(nickname);
            }
        }
        data.addIpAddress(player.getAddress().getAddress());
    }
}
