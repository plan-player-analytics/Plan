package main.java.com.djrapitops.plan.data.handling.importing;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import me.edge209.OnTime.OnTimeAPI;
import me.edge209.OnTime.OnTimeAPI.data;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;

/**
 *
 * @author Rsl1122
 */
public class OnTimeImporter extends Importer {

    /**
     *
     */
    public OnTimeImporter() {
    }

    @Override
    public HandlingInfo importData(UUID uuid) {
        OfflinePlayer p = getOfflinePlayer(uuid);
        Long playTime = OnTimeAPI.getPlayerTimeData(p.getName(), OnTimeAPI.data.TOTALPLAY);
        return new HandlingInfo(uuid, InfoType.OTHER, 0L) {
            @Override
            public boolean process(UserData uData) {
                if (uuid != uData.getUuid()) {
                    return false;
                }
                if (playTime > uData.getPlayTime()) {
                    uData.setPlayTime(playTime);
                    uData.setLastGamemode(GameMode.SURVIVAL);
                    uData.setAllGMTimes(playTime, 0, 0, 0);
                    uData.setLastGmSwapTime(playTime);
                }
                return true;
            }
        };

    }
}
