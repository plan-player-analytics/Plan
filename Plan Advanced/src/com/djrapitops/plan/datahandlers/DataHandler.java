package com.djrapitops.plan.datahandlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.DemographicsData;
import com.djrapitops.plan.database.UserData;
import com.djrapitops.plan.database.ServerData;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        serverData = db.getServerData();
        serverDataHandler = new ServerDataHandler(serverData);
        planLiteHandler = new PlanLiteHandler(plugin);
        
        int minutes = plugin.getConfig().getInt("saveEveryXMinutes");
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                saveHandlerDataToCache();
                saveCachedData();
            }
        }, 60 * 20 * minutes, 60 * 20 * minutes);
    }

    @Deprecated
    public boolean isFirstTimeJoin(UUID uuid) {
        return activityHandler.isFirstTimeJoin(uuid);
    }

    public UserData getCurrentData(UUID uuid, boolean cache) {
        if (!db.wasSeenBefore(uuid)) {
            return null;
        }
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
    }

    public void saveCachedData(UUID uuid) {
        if (dataCache.get(uuid) != null) {
            db.saveUserData(uuid, dataCache.get(uuid));
        }
    }

    public void saveServerData() {
        db.saveServerData(serverData);
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
        dataCache.put(player.getUniqueId(), new UserData(player, new DemographicsData(), db));
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
}
