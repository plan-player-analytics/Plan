package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import java.util.HashMap;
import java.util.UUID;
import static org.bukkit.Bukkit.getPlayer;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class AdvancedAchievementsHook implements Hook {

    private Plan plugin;
    private AdvancedAchievements aAPlugin;
    private int totalAchievements;
    private boolean usingUUID;

    public AdvancedAchievementsHook(Plan plugin) throws Exception, NoClassDefFoundError {
        this.plugin = plugin;
        this.aAPlugin = getPlugin(AdvancedAchievements.class);
        // Version was important because 4.0.3 added required method for Offline players
        String[] aAVersion = aAPlugin.getDescription().getVersion().split(".");
        try {
            double versionNumber = Double.parseDouble(aAVersion[0] + "." + aAVersion[1] + aAVersion[2]);
            if (versionNumber >= 4.03) {
                this.usingUUID = true;
            } else {
                this.usingUUID = false;
                plugin.logError("Advanced Achievements 4.0.3 or above required for Offline players");
            }
        } catch (Exception e) {
            // Some versions were formatted with two numbers
            try {
                double versionNumber = Double.parseDouble(aAVersion[0] + "." + aAVersion[1]);
                if (versionNumber >= 4.03) {
                    this.usingUUID = true;
                } else {
                    this.usingUUID = false;
                    plugin.logError("Advanced Achievements 4.0.3 or above required for Offline players");
                }
            } catch (Exception e2) {
                plugin.logToFile("AAHOOK\nError getting version number.\n" + e2);
            }
        }
        // Get total number of Achievements
        for (NormalAchievements category : NormalAchievements.values()) {
            String categoryName = category.toString();
            if (aAPlugin.getDisabledCategorySet().contains(categoryName)) {
                continue;
            }
            totalAchievements += aAPlugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size();
        }
        for (MultipleAchievements category : MultipleAchievements.values()) {
            String categoryName = category.toString();
            if (aAPlugin.getDisabledCategorySet().contains(categoryName)) {
                continue;
            }
            for (String item : aAPlugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
                totalAchievements += aAPlugin.getPluginConfig().getConfigurationSection(categoryName + '.' + item)
                        .getKeys(false).size();
            }
        }

        if (!aAPlugin.getDisabledCategorySet().contains("Commands")) {
            totalAchievements += aAPlugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false).size();
        }
        //
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        // Check if achievements exist
        if (totalAchievements > 0) {
            UUID uuid = UUIDFetcher.getUUIDOf(player);
            try {
                // Check if correct method is present
                if (this.usingUUID) {
                    data.put("AAC-ACHIEVEMENTS", aAPlugin.getDb().getPlayerAchievementsAmount(uuid.toString()) + " / " + totalAchievements);
                } else {
                    // Fallback method for older versions, only returns Online player data
                    Player p;
                    if (uuid != null) {
                        p = getPlayer(uuid);
                    } else {
                        p = getPlayer(player);
                    }
                    
                    if (p != null) {
                        data.put("AAC-ACHIEVEMENTS", aAPlugin.getDb().getPlayerAchievementsAmount(p) + " / " + totalAchievements);
                    }
                }
            } catch (Exception e) {
                plugin.logToFile("AAHOOK-GetData\nFailed to get data\n" + e + "\nfor: " + player);
            }
        }
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        return getData(player);
    }

    public boolean isUsingUUID() {
        return usingUUID;
    }
}
