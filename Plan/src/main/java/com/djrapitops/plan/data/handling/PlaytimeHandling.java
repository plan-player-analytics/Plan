package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;

public class PlaytimeHandling {

    /**
     * Constructor used to hide the public constructor
     */
    private PlaytimeHandling() {
        throw new IllegalStateException("Utility class");
    }

    public static void processPlaytimeDependentInfo(UserData data, long time, String gamemode, String worldName) {
        long diff = time - data.getLastPlayed();
        long playTime = data.getPlayTime() + diff;
        data.setPlayTime(playTime);
        data.setLastPlayed(time);

        GMTimes gmTimes = data.getGmTimes();
        gmTimes.changeState(gamemode != null ? gamemode : gmTimes.getState(), playTime);

        WorldTimes worldTimes = data.getWorldTimes();
//       TODO worldTimes.changeState(worldName, playTime);
    }
}
