package com.djrapitops.plugin;

import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.systems.TaskCenter;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.velocity.VelocityCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.text.TextComponent;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * (Based on BungeePlugin)
 *
 * @author MicleBrick
 */
public abstract class VelocityPlugin implements IPlugin {

    protected boolean reloading;

    @Override
    public void onEnable() {
        StaticHolder.register(this);
    }

    @Override
    public void onDisable() {
        Class<? extends IPlugin> pluginClass = getClass();
        StaticHolder.unRegister(pluginClass);
        Benchmark.pluginDisabled(pluginClass);
        DebugLog.pluginDisabled(pluginClass);
        TaskCenter.cancelAllKnownTasks(pluginClass);
    }

    @Override
    public void reloadPlugin(boolean full) {
        PluginCommon.reload(this, full);
    }

    @Override
    public void log(String level, String s) {
        Logger logger = getLogger();
        switch (level.toUpperCase()) {
            case "INFO":
            case "I":
                logger.info(s);
                break;
            case "INFO_COLOR":
                getProxy().getConsoleCommandSource().sendMessage(TextComponent.of(s));
                break;
            case "W":
            case "WARN":
            case "WARNING":
                logger.warn(s);
                break;
            case "E":
            case "ERR":
            case "ERROR":
            case "SEVERE":
                logger.error(s);
                break;
            default:
                logger.info(s);
                break;
        }
    }

    public void registerListener(Object... listeners) {
        for (Object listener : listeners) {
            getProxy().getEventManager().register(this, listener);
            StaticHolder.saveInstance(listener.getClass(), getClass());
        }
    }

    @Override
    public void registerCommand(String name, CommandNode command) {
        getProxy().getCommandManager().register(new VelocityCommand(command), name);
        PluginCommon.saveCommandInstances(command, this.getClass());
    }

    protected boolean isNewVersionAvailable(String versionStringUrl) throws IOException {
        return Version.checkVersion(getVersion(), versionStringUrl);
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }

    @Override
    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    protected abstract ProxyServer getProxy();

    protected abstract Logger getLogger();
}
