package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.DemographicsData;
import com.djrapitops.plan.database.UserData;
import com.djrapitops.plan.database.ServerData;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;

public class DataHandler {

    private final HashMap<UUID, UserData> dataCache;
    private final Plan plugin;
    private final ActivityHandler activityHandler;
    private final GamemodeTimesHandler gamemodeTimesHandler;
    private final LocationHandler locationHandler;
    private final DemographicsHandler demographicsHandler;
    private final BasicInfoHandler basicInfoHandler;
    private final RuleBreakingHandler ruleBreakingHandler;
    private final ServerData serverData;
    private final ServerDataHandler serverDataHandler;
    private final PlanLiteHandler planLiteHandler;
    private final Database db;

    private int timesSaved;

    public DataHandler(Plan plugin) {
        this.plugin = plugin;
        db = plugin.getDB();
        dataCache = new HashMap<>();
        activityHandler = new ActivityHandler(plugin, this);
        gamemodeTimesHandler = new GamemodeTimesHandler(plugin, this);
        locationHandler = new LocationHandler(plugin, this);
        demographicsHandler = new DemographicsHandler(plugin, this);
        basicInfoHandler = new BasicInfoHandler(plugin, this);
        ruleBreakingHandler = new RuleBreakingHandler(plugin, this);
        serverData = db.getNewestServerData();
        serverDataHandler = new ServerDataHandler(serverData);
        planLiteHandler = new PlanLiteHandler(plugin);

        timesSaved = 0;

        int minutes = plugin.getConfig().getInt("saveEveryXMinutes");
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                saveHandlerDataToCache();
                saveCachedData();
                if (timesSaved % 5 == 0) {
                    clearCache();
                }
            }
        }, 60 * 20 * minutes, 60 * 20 * minutes);
    }

    @Deprecated
    public boolean isFirstTimeJoin(UUID uuid) {
        return activityHandler.isFirstTimeJoin(uuid);
    }

    public UserData getCurrentData(UUID uuid, boolean cache) {
        if (cache) {
            if (dataCache.get(uuid) == null) {
                dataCache.put(uuid, db.getUserData(uuid));
            }
            return dataCache.get(uuid);
        } else {
            return db.getUserData(uuid);
        }
    }

    public UserData getCurrentData(UUID uuid) {
        return getCurrentData(uuid, true);
    }

    public void saveCachedData() {
        dataCache.keySet().parallelStream().forEach((uuid) -> {
            saveCachedData(uuid);
        });
        saveServerData();
        timesSaved++;
    }

    public void saveCacheOnDisable() {
        dataCache.keySet().stream().forEach((uuid) -> {
            if (dataCache.get(uuid) != null) {
                db.saveUserData(uuid, dataCache.get(uuid));
            }
        });
        db.saveServerData(serverData);
    }

    public void saveCachedData(UUID uuid) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if (dataCache.get(uuid) != null) {
                    db.saveUserData(uuid, dataCache.get(uuid));
                }
            }
        });
    }

    public void saveServerData() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                db.saveServerData(serverData);
            }
        });
    }

    private void saveHandlerDataToCache() {
        Bukkit.getServer().getOnlinePlayers().parallelStream().forEach((p) -> {
            UserData data = getCurrentData(p.getUniqueId());
            activityHandler.saveToCache(p, data);
            gamemodeTimesHandler.saveToCache(p, data);
        });
    }

    public void clearCache() {
        dataCache.clear();
    }

    public void clearFromCache(UUID uuid) {
        if (dataCache.get(uuid) != null) {
            dataCache.remove(uuid);
        }
    }

    public void newPlayer(Player player) {
        UserData data = new UserData(player, new DemographicsData(), db);
        saveCachedData(player.getUniqueId());
        GameMode defaultGM = Bukkit.getServer().getDefaultGameMode();
        if (defaultGM != null) {
            data.setLastGamemode(defaultGM);
        } else {
            data.setLastGamemode(GameMode.SURVIVAL);
        }
        data.setPlayTime(Long.parseLong("0"));
        data.setTimesKicked(0);
        data.setLoginTimes(1);
        data.setLastGmSwapTime(Long.parseLong("0"));
        dataCache.put(player.getUniqueId(), data);
    }

    public ActivityHandler getActivityHandler() {
        return activityHandler;
    }

    public LocationHandler getLocationHandler() {
        return locationHandler;
    }

    public DemographicsHandler getDemographicsHandler() {
        return demographicsHandler;
    }

    public BasicInfoHandler getBasicInfoHandler() {
        return basicInfoHandler;
    }

    public RuleBreakingHandler getRuleBreakingHandler() {
        return ruleBreakingHandler;
    }

    public GamemodeTimesHandler getGamemodeTimesHandler() {
        return gamemodeTimesHandler;
    }

    public Database getDB() {
        return db;
    }

    public ServerData getServerData() {
        return serverData;
    }

    public ServerDataHandler getServerDataHandler() {
        return serverDataHandler;
    }

    public void handleReload() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            boolean newPlayer = activityHandler.isFirstTimeJoin(uuid);
            newPlayer(player);
            serverDataHandler.handleLogin(newPlayer);
            UserData data = getCurrentData(uuid);
            activityHandler.handleReload(player, data);
            basicInfoHandler.handleReload(player, data);
            gamemodeTimesHandler.handleReload(player, data);
            demographicsHandler.handleReload(player, data);
            saveCachedData(uuid);
        }
    }
}
