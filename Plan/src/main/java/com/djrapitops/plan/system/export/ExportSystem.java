package com.djrapitops.plan.system.export;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.file.export.HtmlExport;

/**
 * System in charge of exporting html.
 *
 * @author Rsl1122
 */
public class ExportSystem implements SubSystem {

    private final PlanPlugin plugin;

    public ExportSystem(PlanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void enable() {
        if (Settings.ANALYSIS_EXPORT.isTrue()) {
            Processing.submitNonCritical(new HtmlExport(plugin));
        }
    }

    @Override
    public void disable() {

    }
}