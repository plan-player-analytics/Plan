package com.djrapitops.plan.system.export;

import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.file.export.HtmlExport;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * System in charge of exporting html.
 *
 * @author Rsl1122
 */
@Singleton
public class ExportSystem implements SubSystem {

    private final PlanConfig config;
    private final Processing processing;
    private final HtmlExport htmlExport;

    @Inject
    public ExportSystem(
            PlanConfig config,
            Processing processing,
            HtmlExport htmlExport
    ) {
        this.config = config;
        this.processing = processing;
        this.htmlExport = htmlExport;
    }

    @Override
    public void enable() {
        if (config.isTrue(Settings.ANALYSIS_EXPORT)) {
            processing.submitNonCritical(htmlExport);
        }
    }

    @Override
    public void disable() {

    }
}