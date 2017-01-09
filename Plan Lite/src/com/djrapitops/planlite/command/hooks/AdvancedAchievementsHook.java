package com.djrapitops.planlite.command.hooks;

import com.djrapitops.planlite.api.Hook;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.UUIDFetcher;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.api.DataType;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class AdvancedAchievementsHook implements Hook {

    private PlanLite plugin;
    private AdvancedAchievements aAPlugin;
    private int totalAchievements;
    private boolean usingUUID;

    public AdvancedAchievementsHook(PlanLite plugin) throws Exception, NoClassDefFoundError {
        this.plugin = plugin;
        this.aAPlugin = getPlugin(AdvancedAchievements.class);
        // Version was important because 4.0.3 added required method for Offline players
        String[] aAVersion = aAPlugin.getDescription().getVersion().split("\\.");
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
                plugin.logToFile("AAHOOK\nError getting version number.\n" + e2 + "\n" + e + "\n"
                        + aAPlugin.getDescription().getVersion() + "\n" + Arrays.toString(aAVersion));
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
    }

    @Override
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            UUID uuid = UUIDFetcher.getUUIDOf(player);
            OfflinePlayer p = getOfflinePlayer(uuid);
            if (p.hasPlayedBefore()) {
                if (totalAchievements > 0) {
                    if (this.usingUUID) {
                        data.put("AAC-ACHIEVEMENTS", new DataPoint(aAPlugin.getDb().getPlayerAchievementsAmount(uuid.toString()) + " / " + totalAchievements, DataType.AMOUNT_WITH_MAX));
                    } else {
                        plugin.log("You're using outdated version of AdvancedAchievements!");
                    }
                }
            }
        } catch (IllegalArgumentException e) {
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        return getData(player);
    }

    public boolean isUsingUUID() {
        return usingUUID;
    }
}
