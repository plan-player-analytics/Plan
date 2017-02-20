package main.java.com.djrapitops.plan.data.additional;

import main.java.com.djrapitops.plan.Plan;

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
    
    
}
