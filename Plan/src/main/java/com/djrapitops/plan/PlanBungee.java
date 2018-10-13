/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanBungeeCommand;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plan.utilities.metrics.BStatsBungee;
import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.L;

import java.io.InputStream;

/**
 * Bungee Main class.
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin implements PlanPlugin {

    private PlanSystem system;
    private Locale locale;

    @Override
    public void onEnable() {
        PlanBungeeComponent component = DaggerPlanBungeeComponent.builder().plan(this).build();
        try {
            system = component.system();
            locale = system.getLocaleSystem().getLocale();
            system.enable();

            new BStatsBungee(
                    this,
                    system.getDatabaseSystem().getDatabase(),
                    system.getInfoSystem().getConnectionSystem()
            ).registerMetrics();

            logger.info(locale.getString(PluginLang.ENABLED));
        } catch (AbstractMethodError e) {
            logger.error("Plugin ran into AbstractMethodError - Server restart is required. Likely cause is updating the jar without a restart.");
        } catch (EnableException e) {
            logger.error("----------------------------------------");
            logger.error("Error: " + e.getMessage());
            logger.error("----------------------------------------");
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            onDisable();
        } catch (Exception e) {
            errorHandler.log(L.CRITICAL, this.getClass(), e);
            logger.error("Plugin Failed to Initialize Correctly. If this issue is caused by config settings you can use /planbungee reload");
            logger.error("This error should be reported at https://github.com/Rsl1122/Plan-PlayerAnalytics/issues");
            onDisable();
        }
        PlanBungeeCommand command = component.planCommand();
        command.registerCommands();
        registerCommand("planbungee", command);
    }

    @Override
    public void onDisable() {
        system.disable();

        logger.info(locale.getString(PluginLang.DISABLED));
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }

    @Override
    public void onReload() {
        // Nothing to be done, systems are disabled
    }

    @Override
    public InputStream getResource(String resource) {
        return getResourceAsStream(resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create(system.getConfigSystem().getConfig(), logger);
    }

    @Override
    public PlanSystem getSystem() {
        return system;
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }
}
