package main.java.com.djrapitops.plan.data.importing;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import me.edge209.OnTime.OnTimeAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class OnTimeImporter implements Importer {

    private final Plan plugin;
    private boolean enabled;

    /**
     *
     * @param plugin
     */
    public OnTimeImporter(Plan plugin) {
        this.plugin = plugin;
        this.enabled = Bukkit.getPluginManager().isPluginEnabled("OnTime");
    }

    /**
     *
     * @param uuids
     * @return
     */
    @Override
    public HashMap<UUID, Long> grabNumericData(Set<UUID> uuids) {
        HashMap<UUID, Long> onTimeData = new HashMap<>();
        for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
            Long playTime = OnTimeAPI.getPlayerTimeData(p.getName(), OnTimeAPI.data.TOTALPLAY);
            if (playTime != -1) {
                UUID uuid = p.getUniqueId();
                onTimeData.put(uuid, playTime);                
            }
        }
        return onTimeData;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
