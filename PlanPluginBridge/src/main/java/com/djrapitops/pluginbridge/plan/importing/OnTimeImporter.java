package com.djrapitops.pluginbridge.plan.importing;

import java.util.UUID;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.importing.Importer;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import me.edge209.OnTime.OnTimeAPI;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;

/**
 * Class responsible for importing data from OnTime plugin.
 *
 * Imports playtime
 *
 * @author Rsl1122
 * @since 3.2.0
 */
public class OnTimeImporter extends Importer {

    /**
     * Constructor.
     */
    public OnTimeImporter() {
        super.setInfo("Imports playtime from OnTime & resets GMTimes to survival");
    }

    /**
     * Imports playtime from Ontime.
     *
     * Resets Gamemode times to survival because it is playtime dependent.
     *
     * @param uuid UUID of the player
     * @return HandlingInfo object
     */
    @Override
    public HandlingInfo importData(UUID uuid, String... args) {
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
