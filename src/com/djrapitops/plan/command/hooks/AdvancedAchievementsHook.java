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

    public AdvancedAchievementsHook(Plan plugin) throws Exception, NoClassDefFoundError {
        this.plugin = plugin;
        this.aAPlugin = getPlugin(AdvancedAchievements.class);
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
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        UUID uuid = UUIDFetcher.getUUIDOf(player);
        Player p = getPlayer(player);
        if (uuid != null) {
            p = getPlayer(uuid);
        }
        if (p != null) {
            try {
                if (totalAchievements > 0) {
                    data.put("AAC-ACHIEVEMENTS", aAPlugin.getDb().getPlayerAchievementsAmount(p) + " / " + totalAchievements);
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

}
