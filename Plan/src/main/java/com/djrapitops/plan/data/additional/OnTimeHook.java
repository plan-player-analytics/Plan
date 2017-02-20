package main.java.com.djrapitops.plan.data.additional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import me.edge209.OnTime.OnTime;
import me.edge209.OnTime.OnTimeAPI;
import org.bukkit.Bukkit;

/**
 *
 * @author Rsl1122
 */
public class OnTimeHook extends Hook {

    private final Plan plugin;
    private OnTimeAPI ontimeAPI;

    /**
     * Hooks to OnTime plugin
     * @param plugin
     */
    public OnTimeHook(Plan plugin) {
        super(OnTime.class);
        this.plugin = plugin;        
    }

    /**
     * Grabs information not provided by Player class or Plan from OnTime.
     * isEnabled() should be called before this method.
     *
     * @param uuid UUID of player
     * @return HashMap with boolean, int and string values: VOTES int, REFERRALS int
     */
    public HashMap<String, Serializable> getOnTimeData(UUID uuid) {
        HashMap<String, Serializable> ontimeData = new HashMap<>();
        String name = Bukkit.getOfflinePlayer(uuid).getName();
        ontimeData.put("VOTES", OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALVOTE));
        ontimeData.put("REFERRALS", OnTimeAPI.getPlayerTimeData(name, OnTimeAPI.data.TOTALREFER));
        return ontimeData;
    }
}
