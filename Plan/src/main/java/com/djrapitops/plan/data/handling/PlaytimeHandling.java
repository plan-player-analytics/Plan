package main.java.com.djrapitops.plan.data.handling;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;

public class PlaytimeHandling {

    public static void processPlaytimeDependentInfo(UserData data, long time, String gamemode, String worldName) {
        long diff = time - data.getLastPlayed();
        long playTime = data.getPlayTime() + diff;
        data.setPlayTime(playTime);
        data.setLastPlayed(time);

        GMTimes gmTimes = data.getGmTimes();
        if (gamemode != null) {
            gmTimes.changeState(gamemode, playTime);
        } else {
            gmTimes.changeState(gmTimes.getState(), playTime);
        }

        WorldTimes worldTimes = data.getWorldTimes();
        worldTimes.changeState(worldName, playTime);
    }
}
