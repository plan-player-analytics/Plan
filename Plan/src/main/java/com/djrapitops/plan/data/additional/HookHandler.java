package main.java.com.djrapitops.plan.data.additional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ui.tables.SortableFactionsTableCreator;
import main.java.com.djrapitops.plan.ui.tables.SortableTownTableCreator;

/**
 *
 * @author Rsl1122
 */
public class HookHandler {

    private Plan plan;
    private AdvancedAchievementsHook advancedAchievementsHook;
    private EssentialsHook essentialsHook;
    private SuperbVoteHook superbVoteHook;
    private FactionsHook factionsHook;
    private OnTimeHook onTimeHook;
    private TownyHook townyHook;

    public HookHandler(Plan plan) {
        this.plan = plan;
        hook();
    }

    public void reloadHooks() {
        hook();
    }

    private void hook() {
        advancedAchievementsHook = new AdvancedAchievementsHook(plan);
        essentialsHook = new EssentialsHook(plan);
        superbVoteHook = new SuperbVoteHook(plan);
        factionsHook = new FactionsHook(plan);
        townyHook = new TownyHook(plan);
        onTimeHook = new OnTimeHook(plan);
    }

    public AdvancedAchievementsHook getAdvancedAchievementsHook() {
        return advancedAchievementsHook;
    }

    public EssentialsHook getEssentialsHook() {
        return essentialsHook;
    }

    public SuperbVoteHook getSuperbVoteHook() {
        return superbVoteHook;
    }

    public FactionsHook getFactionsHook() {
        return factionsHook;
    }

    public TownyHook getTownyHook() {
        return townyHook;
    }

    public OnTimeHook getOnTimeHook() {
        return onTimeHook;
    }

    public Map<String, String> getAdditionalAnalysisReplaceRules() {
        Map<String, String> addReplace = new HashMap<>();
        AdvancedAchievementsHook aH = advancedAchievementsHook;
        EssentialsHook eH = essentialsHook;
        SuperbVoteHook sH = superbVoteHook;
        FactionsHook fH = factionsHook;
        TownyHook tH = townyHook;
        addReplace.put("%towntable%", tH.isEnabled() ? SortableTownTableCreator.createSortableTownsTable(tH.getTopTowns(), tH) : "");
        addReplace.put("%factionstable%", fH.isEnabled() ? SortableFactionsTableCreator.createSortableFactionsTable(fH.getTopFactions(), fH) : "");
        addReplace.put("%essentialswarps%", eH.isEnabled() ? "<br/>Warps: "+eH.getWarps().toString() : "");
        return addReplace;
    }

    public Map<String, String> getAdditionalInspectReplaceRules(UUID uuid) {
        Map<String, String> addReplace = new HashMap<>();
        AdvancedAchievementsHook aH = advancedAchievementsHook;
        EssentialsHook eH = essentialsHook;
        SuperbVoteHook sH = superbVoteHook;
        FactionsHook fH = factionsHook;
        TownyHook tH = townyHook;
        addReplace.put("%achievements%", (aH.isEnabled() ? "<br/>Achievements: " + aH.getPlayerAchievements(uuid) + "/" + aH.getTotalAchievements() : ""));
        if (eH.isEnabled()) {
            HashMap<String, Serializable> essData = eH.getEssentialsData(uuid);
            addReplace.put("%essentials%", ((boolean) essData.get("JAILED") ? "| Jailed" : "")
                    + " " + ((boolean) essData.get("MUTED") ? "| Muted" : ""));
        } else {
            addReplace.put("%essentials%", "");
        }

        addReplace.put("%votes%", sH.isEnabled() ? "<br/>Has voted " + sH.getVotes(uuid) + "times" : "");
        if (fH.isEnabled()) {
            HashMap<String, Serializable> facInfo = fH.getPlayerInfo(uuid);
            addReplace.put("%faction%", "<br/>Faction: " + facInfo.get("FACTION") + " | Power: " + facInfo.get("POWER") + "/" + facInfo.get("MAXPOWER"));
        } else {
            addReplace.put("%faction%", "");
        }
        if (tH.isEnabled()) {
            HashMap<String, Serializable> townInfo = tH.getPlayerInfo(uuid);
            addReplace.put("%town%", "<br/>Town: " + townInfo.get("TOWN"));
        } else {
            addReplace.put("%town%", "");
        }
        return addReplace;
    }
}
