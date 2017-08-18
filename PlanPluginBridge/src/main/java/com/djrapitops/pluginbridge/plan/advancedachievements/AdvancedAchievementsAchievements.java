package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * PluginData class for AdvancedAchievements-plugin.
 * <p>
 * Registered to the plugin by AdvancedAchievementsHook.
 * <p>
 * Gives the amount of achievements as value.
 *
 * @author Rsl1122
 * @see AdvancedAchievementsHook
 * @since 3.1.0
 */
public class AdvancedAchievementsAchievements extends PluginData {

    private final AdvancedAchievementsAPI aaAPI;
    private long lastRefresh;
    private Map<UUID, Integer> totalAchievements;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param aaAPI AdvancedAchievementsAPI given by AdvancedAchievementsHook
     */
    public AdvancedAchievementsAchievements(AdvancedAchievementsAPI aaAPI) {
        super("AdvancedAchievements", "achievements", AnalysisType.INT_TOTAL, AnalysisType.INT_AVG);
        this.aaAPI = aaAPI;
        super.setAnalysisOnly(false);
        super.setIcon("check-circle-o");
        super.setPrefix("Achievements: ");
        totalAchievements = aaAPI.getPlayersTotalAchievements();
        lastRefresh = MiscUtils.getTime();
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        if (MiscUtils.getTime() - lastRefresh > 60000) {
            refreshTotalAchievements();
        }
        Integer total = totalAchievements.get(uuid);
        if (total != null) {
            return parseContainer(modifierPrefix, total + "");
        }
        return parseContainer(modifierPrefix, "0");
    }

    private void refreshTotalAchievements() {
        totalAchievements = aaAPI.getPlayersTotalAchievements();
        lastRefresh = MiscUtils.getTime();
    }

    @Override
    public Map<UUID, Serializable> getValues(Collection<UUID> uuid) {
        if (MiscUtils.getTime() - lastRefresh > 60000) {
            refreshTotalAchievements();
        }
        return new HashMap<>(totalAchievements);
    }

    @Override
    public Serializable getValue(UUID uuid) {
        if (MiscUtils.getTime() - lastRefresh > 60000) {
            refreshTotalAchievements();
        }
        Integer total = totalAchievements.get(uuid);
        if (total != null) {
            return total;
        }
        return -1;
    }

}
