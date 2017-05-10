package main.java.com.djrapitops.plan.data.additional.towny;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.API;

/**
 *
 * @author Rsl1122
 */
public class TownyHook extends Hook {

    /**
     * Hooks to Factions plugin
     *
     */
    public TownyHook() throws NoClassDefFoundError {
        super("com.palmergames.bukkit.towny.Towny");
        if (enabled) {
            API planAPI = Plan.getPlanAPI();
            planAPI.addPluginDataSource(new TownyTable(getTopTowns()));
            planAPI.addPluginDataSource(new TownyTown());            
        }
    }

    /**
     * @return List of Faction names sorted by power
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
