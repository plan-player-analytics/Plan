package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.databases.sql.patches.Patch;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;

import java.util.function.Supplier;

/**
 * Task that is in charge on patching the database when the database enables.
 *
 * @author Rsl1122
 */
public class PatchTask extends AbsRunnable {

    private final Patch[] patches;
    private final Supplier<Locale> locale;

    public PatchTask(Patch[] patches, Supplier<Locale> locale) {
        this.patches = patches;
        this.locale = locale;
    }

    @Override
    public void run() {
        try {
            boolean didApply = applyPatches();
            Log.info(locale.get().getString(
                    didApply ? PluginLang.DB_APPLIED_PATCHES : PluginLang.DB_APPLIED_PATCHES_ALREADY
            ));
        } catch (Exception e) {
            Log.error("----------------------------------------------------");
            Log.error(locale.get().getString(PluginLang.ENABLE_FAIL_DB_PATCH));
            Log.error("----------------------------------------------------");
            Log.toLog(this.getClass(), e);
            PlanPlugin.getInstance().onDisable();
        }
    }

    private boolean applyPatches() {
        boolean didApply = false;
        for (Patch patch : patches) {
            if (!patch.hasBeenApplied()) {
                String patchName = patch.getClass().getSimpleName();
                Log.info(locale.get().getString(PluginLang.DB_APPLY_PATCH, patchName));
                patch.apply();
                didApply = true;
            }
        }
        return didApply;
    }

}