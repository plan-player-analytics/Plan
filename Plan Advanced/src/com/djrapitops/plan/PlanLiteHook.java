package com.djrapitops.plan;

import com.djrapitops.plan.api.Hook;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class PlanLiteHook {

    private PlanLite planLite;
    private Plan plugin;

    public PlanLiteHook(Plan plugin) {
        try {
            this.planLite = getPlugin(PlanLite.class);
            if (planLite == null) {
                throw new Exception(Phrase.ERROR_PLANLITE.toString());
            }
        } catch (Exception e) {
            plugin.logError(e.toString());
        }
    }

    void addExtraHook(String name, Hook hook) {
        try {
            if (planLite == null) {
                throw new Exception(Phrase.ERROR_PLANLITE.toString());
            }
            planLite.addExtraHook(name, hook);
            plugin.log(Phrase.PLANLITE_REG_HOOK.toString() + name);
        } catch (Exception | NoClassDefFoundError e) {
            plugin.logError("Failed to hook " + name + "\n  " + e);
        }
    }
}
