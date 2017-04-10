/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main.java.com.djrapitops.plan.data.handling;

import java.util.HashMap;
import main.java.com.djrapitops.plan.data.UserData;
import org.bukkit.GameMode;

/**
 *
 * @author Risto
 */
public class GamemodeHandling {
    public static void processGamemodeInfo(UserData data, long time, GameMode newGM) {
        if (newGM == null) {
            return;
        }
        
        GameMode lastGamemode = data.getLastGamemode();
        if (lastGamemode == null) {
            data.setLastGamemode(newGM);
        }
        lastGamemode = data.getLastGamemode();
        HashMap<GameMode, Long> times = data.getGmTimes();
        Long currentGMTime = times.get(lastGamemode);
        if (currentGMTime == null) {
            currentGMTime = 0L;
        }
        data.setPlayTime(data.getPlayTime() + (time - data.getLastPlayed()));
        data.setLastPlayed(time);
        long lastSwap = data.getLastGmSwapTime();
        long playtime = data.getPlayTime();
        data.setGMTime(lastGamemode, currentGMTime + (playtime - lastSwap));
        data.setLastGmSwapTime(playtime);
        data.setLastGamemode(newGM);
    }
}
