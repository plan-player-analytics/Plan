package main.java.com.djrapitops.plan.data.additional.factions;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

/**
 *
 * @author Rsl1122
 */
public class FactionsHook extends Hook {

    /**
     * Hooks to Factions plugin
     *
     */
    public FactionsHook(HookHandler hookH) {
        super("com.massivecraft.factions.Factions");
        if (enabled) {            
            hookH.addPluginDataSource(new FactionsFaction());
            hookH.addPluginDataSource(new FactionsPower());
            hookH.addPluginDataSource(new FactionsMaxPower());
            hookH.addPluginDataSource(new FactionsTable(this.getTopFactions()));
        }
    }

    /**
     * @return List of Faction names sorted by power
     */
    public List<Faction> getTopFactions() {
        List<Faction> topFactions = new ArrayList<>();
        topFactions.addAll(FactionColl.get().getAll());
        topFactions.remove(FactionColl.get().getWarzone());
        topFactions.remove(FactionColl.get().getSafezone());
        topFactions.remove(FactionColl.get().getNone());
        List<String> hide = Settings.HIDE_FACTIONS.getStringList();
        Collections.sort(topFactions, new FactionComparator());
        List<Faction> factionNames = topFactions.stream()
                .filter(faction -> !hide.contains(faction.getName()))
                .collect(Collectors.toList());
        return factionNames;
    }
}
