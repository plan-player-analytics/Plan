/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.db.tasks;

import com.djrapitops.plan.db.patches.Patch;
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
@Deprecated
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