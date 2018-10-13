package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.system.database.databases.sql.patches.Patch;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

/**
 * Task that is in charge on patching the database when the database enables.
 *
 * @author Rsl1122
 */
public class PatchTask extends AbsRunnable {

    private final Patch[] patches;

    private final Locale locale;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    public PatchTask(Patch[] patches, Locale locale, PluginLogger logger, ErrorHandler errorHandler) {
        this.patches = patches;
        this.locale = locale;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        try {
            boolean applied = applyPatches();
            logger.info(locale.getString(
                    applied ? PluginLang.DB_APPLIED_PATCHES : PluginLang.DB_APPLIED_PATCHES_ALREADY
            ));
        } catch (Exception e) {
            logger.error("----------------------------------------------------");
            logger.error(locale.getString(PluginLang.ENABLE_FAIL_DB_PATCH));
            logger.error("----------------------------------------------------");
            errorHandler.log(L.CRITICAL, this.getClass(), e);
        }
    }

    private boolean applyPatches() {
        boolean didApply = false;
        for (Patch patch : patches) {
            if (!patch.hasBeenApplied()) {
                String patchName = patch.getClass().getSimpleName();
                logger.info(locale.getString(PluginLang.DB_APPLY_PATCH, patchName));
                patch.apply();
                didApply = true;
            }
        }
        return didApply;
    }

}