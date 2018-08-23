package com.djrapitops.plan.system.export;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.file.export.HtmlExport;

import javax.inject.Inject;

/**
 * System in charge of exporting html.
 *
 * @author Rsl1122
 */
public class ExportSystem implements SubSystem {

    private PlanConfig config;
    private HtmlExport htmlExport;

    @Inject
    public ExportSystem(
            PlanConfig config,
            HtmlExport htmlExport
    ) {
        this.config = config;
        this.htmlExport = htmlExport;
    }

    @Override
    public void enable() {
        if (config.isTrue(Settings.ANALYSIS_EXPORT)) {
            Processing.submitNonCritical(htmlExport);
        }
    }

    @Override
    public void disable() {

    }
}