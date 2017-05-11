package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.data.additional.essentials.EssentialsHook;
import main.java.com.djrapitops.plan.data.additional.advancedachievements.AdvancedAchievementsHook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.factions.FactionsHook;
import main.java.com.djrapitops.plan.data.additional.ontime.OnTimeHook;
import main.java.com.djrapitops.plan.data.additional.towny.TownyHook;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * Class responsible for hooking to other plugins & managing the %plugins%
 * placeholder on Analysis & Inspect pages.
 *
 * @author Rsl1122
 */
public class HookHandler {

    private List<PluginData> additionalDataSources;
    private AdvancedAchievementsHook advancedAchievementsHook;
    private EssentialsHook essentialsHook;
    private FactionsHook factionsHook;
    private OnTimeHook onTimeHook;
    private TownyHook townyHook;

    /**
     * Class constructor, hooks to plugins.
     *
     * @param plan Current instance of Plan.
     */
    public HookHandler() {
        additionalDataSources = new ArrayList<>();
        hook();
    }

    /**
     * Adds a new PluginData source to the list.
     *
     * The plugin data will appear on Analysis and/or Inspect pages depending on
     * how the extending object is set up.
     *
     * Refer to documentation on github for more information.
     *
     * @param dataSource an object extending the PluginData class.
     */
    public void addPluginDataSource(PluginData dataSource) {
        Log.debug("Registered a new datasource: " + dataSource.getPlaceholder("").replace("%", ""));
        additionalDataSources.add(dataSource);
    }

    /**
     * Used to get all PluginData objects in a list.
     *
     * @return List of PluginData objects.
     */
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
            advancedAchievementsHook = new AdvancedAchievementsHook(this);
        } catch (NoClassDefFoundError e) {
        }
        try {
            essentialsHook = new EssentialsHook(this);
        } catch (NoClassDefFoundError e) {
        }
        try {
            factionsHook = new FactionsHook(this);
        } catch (NoClassDefFoundError e) {
        }
        try {
            onTimeHook = new OnTimeHook(this);
        } catch (NoClassDefFoundError e) {
        }
        try {
            townyHook = new TownyHook(this);
        } catch (NoClassDefFoundError e) {
        }
    }

    public String getPluginsTabLayoutForAnalysis() {
        List<String> pluginNames = getPluginNamesAnalysis();
        Map<String, List<String>> placeholders = getPlaceholdersAnalysis();
        return HtmlUtils.getPluginsTabLayout(pluginNames, placeholders);
    }

    public String getPluginsTabLayoutForInspect() {
        List<String> pluginNames = getPluginNamesInspect();
        Map<String, List<String>> placeholders = getPlaceholdersInspect();
        return HtmlUtils.getPluginsTabLayout(pluginNames, placeholders);
    }

    private List<String> getPluginNamesAnalysis() {
        List<String> pluginNames = additionalDataSources.stream()
                .filter(source -> !source.getAnalysisTypes().isEmpty())
                .map(source -> source.getSourcePlugin())
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(pluginNames);
        return pluginNames;
    }

    private List<String> getPluginNamesInspect() {
        List<String> pluginNames = additionalDataSources.stream()
                .filter(source -> !source.analysisOnly())
                .map(source -> source.getSourcePlugin())
                .distinct()
                .collect(Collectors.toList());
        Collections.sort(pluginNames);
        return pluginNames;
    }

    private Map<String, List<String>> getPlaceholdersAnalysis() {
        Map<String, List<String>> placeholders = new HashMap<>();
        for (PluginData source : additionalDataSources) {
            List<AnalysisType> analysisTypes = source.getAnalysisTypes();
            if (analysisTypes.isEmpty()) {
                continue;
            }
            String pluginName = source.getSourcePlugin();
            if (!placeholders.containsKey(pluginName)) {
                placeholders.put(pluginName, new ArrayList<>());
            }
            for (AnalysisType t : analysisTypes) {
                placeholders.get(pluginName).add(source.getPlaceholder(t.getPlaceholderModifier()));
            }
        }
        return placeholders;
    }

    private Map<String, List<String>> getPlaceholdersInspect() {
        Map<String, List<String>> placeholders = new HashMap<>();
        for (PluginData source : additionalDataSources) {
            if (source.analysisOnly()) {
                continue;
            }
            String pluginName = source.getSourcePlugin();
            if (!placeholders.containsKey(pluginName)) {
                placeholders.put(pluginName, new ArrayList<>());
            }
            placeholders.get(pluginName).add(source.getPlaceholder(""));
        }
        return placeholders;
    }

    /**
     * Used to get the replaceMap for inspect page.
     *
     * @param uuid UUID of the player whose page is being inspected.
     * @return Map: key|value - %placeholder%|value
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
