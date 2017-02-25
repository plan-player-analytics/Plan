package main.java.com.djrapitops.plan.data.additional;

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
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
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
        super("com.massivecraft.factions.Factions");
        this.plugin = plugin;
    }

    public FactionsHook() {
        super();
        plugin = null;
    }

    /**
     * @return List of Faction names sorted by power
     */
    public List<String> getTopFactions() {
        List<Faction> topFactions = new ArrayList<>();
        topFactions.addAll(FactionColl.get().getAll());
        topFactions.remove(FactionColl.get().getWarzone());
        topFactions.remove(FactionColl.get().getSafezone());
        topFactions.remove(FactionColl.get().getNone());
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
     * String, LAND int
     */
    public HashMap<String, Serializable> getFactionInfo(String factionName) {
        HashMap<String, Serializable> info = new HashMap<>();
        Faction faction = FactionColl.get().getByName(factionName);
        if (faction != null) {
            MPlayer leader = faction.getLeader();
            if (leader != null) {
                info.put("LEADER", leader.getNameAndSomething("", ""));
            } else {
                info.put("LEADER", Html.FACTION_NO_LEADER.parse());
            }
            
            info.put("POWER", FormatUtils.cutDecimals(faction.getPower()));
            info.put("LAND", faction.getLandCount());
        } else {
            info.put("LEADER", Html.FACTION_NOT_FOUND.parse());
            info.put("POWER", Html.FACTION_NOT_FOUND.parse());
            info.put("LAND", Html.FACTION_NOT_FOUND.parse());
        }
        return info;
    }

    /**
     * Grab info about a Player. isEnabled() should be called before this
     * method.
     *
     * @param uuid UUID of the player
     * @return HashMap containing boolean, number & string: POWER int, MAXPOWER
     * int, FACTION String
     */
    public HashMap<String, Serializable> getPlayerInfo(UUID uuid) {
        HashMap<String, Serializable> info = new HashMap<>();
        MPlayer mPlayer = MPlayer.get(uuid);
        if (mPlayer != null) {
            info.put("POWER", FormatUtils.cutDecimals(mPlayer.getPower()));
            info.put("MAXPOWER", mPlayer.getPowerMax());
            if (mPlayer.hasFaction()) {
                info.put("FACTION", mPlayer.getFactionName());
            } else {
                info.put("FACTION", Phrase.NOT_IN_FAC + "");
            }
        } else {
            info.put("POWER", 0);
            info.put("MAXPOWER", 0);
            info.put("FACTION", Phrase.NOT_IN_FAC + "");
        }
        return info;
    }
}
