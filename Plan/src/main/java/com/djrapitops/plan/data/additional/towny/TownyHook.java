package main.java.com.djrapitops.plan.data.additional.towny;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

/**
 * A Class responsible for hooking to Towny and registering 2 data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class TownyHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public TownyHook(HookHandler hookH) throws NoClassDefFoundError {
        super("com.palmergames.bukkit.towny.Towny");
        if (enabled) {
            hookH.addPluginDataSource(new TownyTable(getTopTowns()));
            hookH.addPluginDataSource(new TownyTown());
        }
    }

    /**
     * Used to get the list of Towns and filter out unnessecary ones.
     *
     * @return List of Towns sorted by amount of residents.
     */
    public List<Town> getTopTowns() {
        List<Town> topTowns = TownyUniverse.getDataSource().getTowns();
        Collections.sort(topTowns, new TownComparator());
        List<String> hide = Settings.HIDE_TOWNS.getStringList();
        List<Town> townNames = topTowns.stream()
                .filter(town -> !hide.contains(town.getName()))
                .collect(Collectors.toList());
        return townNames;
    }
}
