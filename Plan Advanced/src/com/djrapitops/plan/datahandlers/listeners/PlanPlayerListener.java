package com.djrapitops.plan.datahandlers.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.datahandlers.ActivityHandler;
import com.djrapitops.plan.datahandlers.DataHandler;
import com.djrapitops.plan.datahandlers.DemographicsHandler;
import com.djrapitops.plan.datahandlers.LocationHandler;
import com.djrapitops.plan.datahandlers.RuleBreakingHandler;
import com.djrapitops.plan.datahandlers.ServerDataHandler;
import com.djrapitops.plan.database.UserData;
import com.djrapitops.plan.datahandlers.BasicInfoHandler;
import com.djrapitops.plan.datahandlers.GamemodeTimesHandler;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlanPlayerListener implements Listener {

    private final DataHandler handler;
    private final ActivityHandler activityH;
    private final BasicInfoHandler basicInfoH;
    private final GamemodeTimesHandler gmTimesH;
    private final LocationHandler locationH;
    private final DemographicsHandler demographicH;
    private final RuleBreakingHandler rulebreakH;
    private final ServerDataHandler serverHandler;

    public PlanPlayerListener(Plan plugin) {
        handler = plugin.getHandler();
        activityH = handler.getActivityHandler();
        basicInfoH = handler.getBasicInfoHandler();
        gmTimesH = handler.getGamemodeTimesHandler();
        demographicH = handler.getDemographicsHandler();
        locationH = handler.getLocationHandler();
        rulebreakH = handler.getRuleBreakingHandler();
        serverHandler = handler.getServerDataHandler();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (!event.getResult().equals(Result.ALLOWED)) {
            return;
        }
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean newPlayer = activityH.isFirstTimeJoin(uuid);
        handler.newPlayer(player);
        serverHandler.handleLogin(newPlayer);
        UserData data = handler.getCurrentData(uuid);
        activityH.handleLogIn(event, data);
        basicInfoH.handleLogIn(event, data);
        gmTimesH.handleLogin(event, data);
        demographicH.handleLogIn(event, data);
        handler.saveCachedData(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        UserData data = handler.getCurrentData(uuid);
        activityH.handleLogOut(event, data);
        locationH.handleLogOut(event, data);
        serverHandler.handleLogout();
        handler.saveCachedData(uuid);
        handler.clearFromCache(uuid);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        UserData data = handler.getCurrentData(uuid);
        rulebreakH.handleKick(event, data);
        serverHandler.handleKick();
        handler.saveCachedData(uuid);
        handler.clearFromCache(uuid);
    }
}
