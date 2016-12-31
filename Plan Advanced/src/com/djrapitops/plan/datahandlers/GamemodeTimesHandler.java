package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.UserData;
import java.util.HashMap;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class GamemodeTimesHandler {

    private final Plan plugin;
    private final DataHandler handler;

    public GamemodeTimesHandler(Plan plugin, DataHandler h) {
        this.plugin = plugin;
        handler = h;
    }

    public void handleLogin(PlayerLoginEvent event, UserData data) {
        data.setLastGamemode(event.getPlayer().getGameMode());
    }

    public void handleChangeEvent(PlayerGameModeChangeEvent event, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        long lastSwap = data.getLastGmSwapTime();
        handler.getActivityHandler().saveToCache(event.getPlayer(), data);
        long now = data.getPlayTime();
        GameMode oldGM = data.getLastGamemode();
        data.setGMTime(oldGM, times.get(oldGM) + (lastSwap - now));
        GameMode newGM = event.getNewGameMode();
        data.setLastGamemode(newGM);
        data.setLastGmSwapTime(now);
    }

    void saveToCache(Player p, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        long lastSwap = data.getLastGmSwapTime();
        handler.getActivityHandler().saveToCache(p, data);
        long now = data.getPlayTime();
        GameMode currentGM = p.getGameMode();
        data.setGMTime(currentGM, times.get(currentGM) + (lastSwap - now));
        data.setLastGmSwapTime(now);
    }

    void handleReload(Player p, UserData data) {
        HashMap<GameMode, Long> times = data.getGmTimes();
        long lastSwap = data.getLastGmSwapTime();
        handler.getActivityHandler().saveToCache(p, data);
        long now = data.getPlayTime();
        GameMode currentGM = p.getGameMode();
        data.setGMTime(currentGM, times.get(currentGM) + (lastSwap - now));
        data.setLastGmSwapTime(now);
    }

}
