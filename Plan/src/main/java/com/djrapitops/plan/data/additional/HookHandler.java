package main.java.com.djrapitops.plan.data.additional;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ui.Html;
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

    /**
     *
     * @param plan
     */
    public HookHandler(Plan plan) {
        this.plan = plan;
        hook();
    }

    /**
     *
     */
    public void reloadHooks() {
        hook();
    }

    private void hook() {
        try {
            advancedAchievementsHook = new AdvancedAchievementsHook(plan);
        } catch (NoClassDefFoundError e) {
            advancedAchievementsHook = new AdvancedAchievementsHook();
        }
        try {
            essentialsHook = new EssentialsHook(plan);
        } catch (NoClassDefFoundError e) {
            essentialsHook = new EssentialsHook();
        }
        try {
            superbVoteHook = new SuperbVoteHook(plan);
        } catch (NoClassDefFoundError e) {
            superbVoteHook = new SuperbVoteHook();
        }
        try {
            factionsHook = new FactionsHook(plan);
        } catch (NoClassDefFoundError e) {
            factionsHook = new FactionsHook();
        }
        try {
            townyHook = new TownyHook(plan);
        } catch (NoClassDefFoundError e) {
            townyHook = new TownyHook();
        }
        try {
            onTimeHook = new OnTimeHook(plan);
        } catch (NoClassDefFoundError e) {
            onTimeHook = new OnTimeHook();
        }
    }

    /**
     *
     * @return
     */
    public AdvancedAchievementsHook getAdvancedAchievementsHook() {
        return advancedAchievementsHook;
    }

    /**
     *
     * @return
     */
    public EssentialsHook getEssentialsHook() {
        return essentialsHook;
    }

    /**
     *
     * @return
     */
    public SuperbVoteHook getSuperbVoteHook() {
        return superbVoteHook;
    }

    /**
     *
     * @return
     */
    public FactionsHook getFactionsHook() {
        return factionsHook;
    }

    /**
     *
     * @return
     */
    public TownyHook getTownyHook() {
        return townyHook;
    }

    /**
     *
     * @return
     */
    public OnTimeHook getOnTimeHook() {
        return onTimeHook;
    }

    /**
     *
     * @return
     */
    public Map<String, String> getAdditionalAnalysisReplaceRules() {
        Map<String, String> addReplace = new HashMap<>();
        AdvancedAchievementsHook aH = advancedAchievementsHook;
        EssentialsHook eH = essentialsHook;
        SuperbVoteHook sH = superbVoteHook;
        FactionsHook fH = factionsHook;
        TownyHook tH = townyHook;
        addReplace.put("%towntable%", tH.isEnabled() ? SortableTownTableCreator.createSortableTownsTable(tH.getTopTowns(), tH) : "");
        addReplace.put("%factionstable%", fH.isEnabled() ? SortableFactionsTableCreator.createSortableFactionsTable(fH.getTopFactions(), fH) : "");
        addReplace.put("%essentialswarps%", eH.isEnabled() ?  Html.WARPS.parse(eH.getWarps().toString()) : "");
        return addReplace;
    }

    /**
     *
     * @param uuid
     * @return
     */
    public Map<String, String> getAdditionalInspectReplaceRules(UUID uuid) {
        Map<String, String> addReplace = new HashMap<>();
        AdvancedAchievementsHook aH = advancedAchievementsHook;
        EssentialsHook eH = essentialsHook;
        SuperbVoteHook sH = superbVoteHook;
        FactionsHook fH = factionsHook;
        TownyHook tH = townyHook;
        addReplace.put("%achievements%", (aH.isEnabled() ? Html.ACHIEVEMENTS.parse(aH.getPlayerAchievements(uuid)+"",aH.getTotalAchievements()+"") : ""));
        if (eH.isEnabled()) {
            HashMap<String, Serializable> essData = eH.getEssentialsData(uuid);
            addReplace.put("%essentials%", ((boolean) essData.get("JAILED") ? Html.JAILED.parse() : "")
                    + " " + ((boolean) essData.get("MUTED") ? Html.MUTED.parse() : ""));
        } else {
            addReplace.put("%essentials%", "");
        }

        addReplace.put("%votes%", sH.isEnabled() ? Html.VOTES.parse(sH.getVotes(uuid)+"") : "");
        if (fH.isEnabled()) {
            HashMap<String, Serializable> facInfo = fH.getPlayerInfo(uuid);
            addReplace.put("%faction%", Html.FACTION.parse(facInfo.get("FACTION")+"",facInfo.get("POWER")+"",facInfo.get("MAXPOWER")+""));
        } else {
            addReplace.put("%faction%", "");
        }
        if (tH.isEnabled()) {
            HashMap<String, Serializable> townInfo = tH.getPlayerInfo(uuid);
            addReplace.put("%town%", Html.TOWN.parse(townInfo.get("TOWN")+""));
        } else {
            addReplace.put("%town%", "");
        }
        return addReplace;
    }
}
