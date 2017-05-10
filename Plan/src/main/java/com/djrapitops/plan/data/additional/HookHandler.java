package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.data.additional.essentials.EssentialsHook;
import main.java.com.djrapitops.plan.data.additional.advancedachievements.AdvancedAchievementsHook;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.factions.FactionsHook;
import main.java.com.djrapitops.plan.data.additional.ontime.OnTimeHook;
import main.java.com.djrapitops.plan.data.additional.towny.TownyHook;

/**
 *
 * @author Rsl1122
 */
public class HookHandler {

    private Plan plan;
    private List<PluginData> additionalDataSources;
    private AdvancedAchievementsHook advancedAchievementsHook;
    private EssentialsHook essentialsHook;
    private FactionsHook factionsHook;
    private OnTimeHook onTimeHook;
    private TownyHook townyHook;

    /**
     *
     * @param plan
     */
    public HookHandler(Plan plan) {
        this.plan = plan;
        additionalDataSources = new ArrayList<>();
        hook();
    }

    public void addPluginDataSource(PluginData dataSource) {
        additionalDataSources.add(dataSource);
    }

    public List<PluginData> getAdditionalDataSources() {
        return additionalDataSources;
    }
    
    /**
     *
     */
    public void reloadHooks() {
        additionalDataSources.clear();
        hook();
    }

    private void hook() {
        try {
            advancedAchievementsHook = new AdvancedAchievementsHook();
        } catch (NoClassDefFoundError e) {
        }
        try {
            essentialsHook = new EssentialsHook();
        } catch (NoClassDefFoundError e) {
        }
        try {
            factionsHook = new FactionsHook();
        } catch (NoClassDefFoundError e) {
        }
        try {
            onTimeHook = new OnTimeHook();
        } catch (NoClassDefFoundError e) {
        }
        try {
            townyHook = new TownyHook();
        } catch (NoClassDefFoundError e) {
        }
    }

    /**
     *
     * @param uuid
     * @return
     */
    public Map<String, String> getAdditionalInspectReplaceRules(UUID uuid) {
        Map<String, String> addReplace = new HashMap<>();
        for (PluginData source : additionalDataSources) {
            if (source.analysisOnly()) {
                continue;
            }
            addReplace.put(source.getPlaceholder(""), source.getHtmlReplaceValue("", uuid));
        }
        return addReplace;
    }
}
