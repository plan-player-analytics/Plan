/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanBungeeCommand;
import com.djrapitops.plan.system.BungeeSystem;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.system.settings.theme.PlanColorScheme;
import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;

import java.io.InputStream;

/**
 * Bungee Main class.
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin implements PlanPlugin {

    private BungeeSystem system;

    public static PlanBungee getInstance() {
        return (PlanBungee) StaticHolder.getInstance(PlanBungee.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            system = new BungeeSystem(this);
            system.enable();

            Log.info(Locale.get(Msg.ENABLED).toString());
        } catch (Exception e) {
            Log.error("Plugin Failed to Initialize Correctly:");
            Log.toLog(this.getClass(), e);
        }
        registerCommand("planbungee", new PlanBungeeCommand(this));
    }

    @Override
    public void onDisable() {
        system.disable();

        Log.info(Locale.get(Msg.DISABLED).toString());
        Benchmark.pluginDisabled(PlanBungee.class);
        DebugLog.pluginDisabled(PlanBungee.class);
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }

    @Override
    public void onReload() {
    }

    @Override
    public InputStream getResource(String resource) {
        return getResourceAsStream(resource);
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
    }

    public BungeeSystem getSystem() {
        return system;
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }
}