package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.PlaytimeHandling;

import java.util.UUID;

public class PlaytimeDependentInfo extends HandlingInfo {

    private final String gamemode;
    private final String worldName;

    public PlaytimeDependentInfo(UUID uuid, InfoType type, long time, String gm, String worldName) {
        super(uuid, type, time);
        this.worldName = worldName;
        this.gamemode = gm;
    }

    @Override
    public boolean process(UserData uData) {
        if (!uuid.equals(uData.getUuid())) {
            return false;
        }
        PlaytimeHandling.processPlaytimeDependentInfo(uData, time, gamemode, worldName);
        return true;
    }
}
