package main.java.com.djrapitops.plan.data.additional;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.comparators.FactionComparator;

/**
 *
 * @author Rsl1122
 */
public class FactionsHook extends Hook {

    private final Plan plugin;

    /**
     * Hooks to Factions plugin
     *
     * @param plugin
     */
    public FactionsHook(Plan plugin) {
        super(Factions.class);
        this.plugin = plugin;
    }

    /**
     * @return List of Faction names sorted by power
     */
    public List<String> getTopFactions() {
        List<Faction> topFactions = new ArrayList<>();
        topFactions.addAll(FactionColl.get().getAll());
        Collections.sort(topFactions, new FactionComparator());
        List<String> factionNames = topFactions.stream()
                .map(faction -> faction.getName())
                .collect(Collectors.toList());
        return factionNames;
    }

    /**
     * Grab basic info about Faction. isEnabled() should be called before this
     * method.
     *
     * @param factionName Name of the faction.
     * @return HashMap containing boolean, number & string: LEADER String, POWER
     * double, LAND int
     */
    public HashMap<String, Serializable> getFactionInfo(String factionName) {
        HashMap<String, Serializable> info = new HashMap<>();
        Faction faction = FactionColl.get().getByName(factionName);
        info.put("LEADER", faction.getLeader().getNameAndSomething("", ""));
        info.put("POWER", faction.getPower());
        info.put("LAND", faction.getPower());
        return info;
    }
    
    /**
     * Grab power of player. isEnabled() should be called before this
     * method.
     * @param uuid UUID of player
     * @return Faction power of player
     */
    public double getPower(UUID uuid) {
        return MPlayer.get(uuid).getPower();
    }

    /**
     * Grab Max power of player. isEnabled() should be called before this
     * method.
     * @param uuid UUID of player
     * @return Faction Max power of player
     */
    public double getMaxPower(UUID uuid) {
        return MPlayer.get(uuid).getPowerMax();
    }
}
