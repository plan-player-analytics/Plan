package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.UserData;
import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class GamemodeTimesHandler {

    private final Plan plugin;
    private final DataHandler handler;

    public GamemodeTimesHandler(Plan plugin, DataHandler h) {
        this.plugin = plugin;
        handler = h;
    }

    public void handleLogin(PlayerJoinEvent event, UserData data) {
        data.setLastGamemode(event.getPlayer().getGameMode());
    }

    public void handleChangeEvent(PlayerGameModeChangeEvent event, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(event.getPlayer(), data);

        long lastSwap = data.getLastGmSwapTime();
        long playTime = data.getPlayTime();
        GameMode oldGM = data.getLastGamemode();
        data.setGMTime(oldGM, times.get(oldGM) + (playTime - lastSwap));

        GameMode newGM = event.getNewGameMode();
        data.setLastGamemode(newGM);

        data.setLastGmSwapTime(playTime);
    }

    void saveToCache(Player p, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(p, data);

        long lastSwap = data.getLastGmSwapTime();
        long playtime = data.getPlayTime();
        GameMode currentGM = p.getGameMode();
        data.setGMTime(currentGM, times.get(currentGM) + (playtime - lastSwap));

        data.setLastGmSwapTime(playtime);
    }

    void handleReload(Player p, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        handler.getActivityHandler().saveToCache(p, data);

        long lastSwap = data.getLastGmSwapTime();
        long playTime = data.getPlayTime();
        GameMode currentGM = p.getGameMode();
        data.setGMTime(currentGM, times.get(currentGM) + (playTime - lastSwap));
        
        data.setLastGmSwapTime(playTime);
    }

}
