package com.djrapitops.plan.data.handlers;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.data.UserData;
import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.planlite.api.DataPoint;
import java.util.HashMap;
import java.util.Set;
import main.java.com.djrapitops.plan.data.PlanLitePlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlanLiteHandler {

    private Plan plugin;
    private PlanLiteHook hook;
    private DataCacheHandler handler;
    private boolean enabled;

    public PlanLiteHandler(Plan plugin) {
        this.plugin = plugin;
        PlanLiteHook planLiteHook = plugin.getPlanLiteHook();
        enabled = planLiteHook.isEnabled();
        if (enabled) {
            hook = planLiteHook;
        }
    }

    public void handleLogin(PlayerJoinEvent event, UserData data) {
        if (!enabled) {
            data.setPlanLiteFound(false);
            return;
        }
        Player p = event.getPlayer();
        String playerName = p.getName();
        
        handleEvents(playerName, data);
    }

    public void handleEvents(String playerName, UserData data) {
        if (!enabled) {
            return;
        }
        Set<String> enabledHooks = hook.getEnabledHooksNames();
        HashMap<String, DataPoint> liteData = hook.getAllData(playerName, true);
        PlanLitePlayerData plData = new PlanLitePlayerData();

        plData.setTowny(enabledHooks.contains("Towny"));
        plData.setFactions(enabledHooks.contains("Factions"));
        plData.setSuperbVote(enabledHooks.contains("SuperbVote"));
        plData.setVault(enabledHooks.contains("Vault"));
        if (plData.hasTowny()) {
            DataPoint town = liteData.get("TOW-TOWN");
            plData.setTown((town != null) ? town.data() : "Not in a town");
            plData.setFriends(liteData.get("TOW-FRIENDS").data());
            plData.setPlotPerms(liteData.get("TOW-PLOT PERMS").data());
            plData.setPlotOptions(liteData.get("TOW-PLOT OPTIONS").data());
        }
        if (plData.hasFactions()) {
            DataPoint faction = liteData.get("FAC-FACTION");
            plData.setFaction((faction != null) ? faction.data() : "Not in a faction");
        }
        if (plData.hasSuperbVote()) {
            try {
                plData.setVotes(Integer.parseInt(liteData.get("SVO-VOTES").data()));
            } catch (Exception e) {
                plData.setVotes(0);
            }
        }
        if (plData.hasVault()) {
            try {
                plData.setMoney(Double.parseDouble(FormatUtils.removeLetters(liteData.get("ECO-BALANCE").data())));
            } catch (Exception e) {
                plData.setMoney(0);
            }
        }
        data.setPlanLiteFound(true);
        data.setPlanLiteData(plData);
    }
}
