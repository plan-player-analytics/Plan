package main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;

import java.util.UUID;

@Deprecated //Sessions will take care of the stuff in the future.
public class PlaytimeDependentInfo extends HandlingInfo {

    private final String gamemode;
    private final String worldName;

    public PlaytimeDependentInfo(UUID uuid, InfoType type, long time, String gm, String worldName) {
        super(uuid, type, time);
        this.worldName = worldName;
        this.gamemode = gm;
    }

    @Override
    public void process(UserData uData) {
        if (!uuid.equals(uData.getUuid())) {
            return;
        }
    }
}
