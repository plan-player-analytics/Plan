/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Risto Lahtela
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.djrapitops.plugin;

import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.nukkit.NukkitCMDSender;
import com.djrapitops.plugin.logging.console.NukkitPluginLogger;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.debug.CombineDebugLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.debug.MemoryDebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.task.nukkit.NukkitRunnableFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * APFPlugin implementation for Nukkit.
 *
 * @author Rsl1122
 */
public class NukkitPlugin extends PluginBase implements APFPlugin {

    protected final CombineDebugLogger debugLogger;
    protected final Timings timings;
    protected final RunnableFactory runnableFactory;
    private final Map<String, CommandNode> commands;
    protected PluginLogger logger;
    protected boolean reloading;

    /**
     * Standard constructor that initializes the plugin with the default DebugLogger.
     */
    public NukkitPlugin() {
        this(new CombineDebugLogger(new MemoryDebugLogger()));
    }

    /**
     * Constructor for defining a debug logger at creation time.
     *
     * @param debugLogger debug logger to use.
     */
    public NukkitPlugin(CombineDebugLogger debugLogger) {
        this.debugLogger = debugLogger;
        this.runnableFactory = new NukkitRunnableFactory(this);
        this.timings = new Timings(debugLogger);
        this.logger = new NukkitPluginLogger(
                message -> getServer().getConsoleSender().sendMessage(message),
                this::getDebugLogger,
                this::getLogger
        );
        commands = new HashMap<>();
    }

    @Override
    public void onDisable() {
        runnableFactory.cancelAllKnownTasks();
    }

    public void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            if (listener == null) {
                logger.warn("Attempted to register a null listener!");
                continue;
            }
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    @Override
    public void registerCommand(String name, CommandNode command) {
        if (command == null) {
            logger.warn("Attempted to register a null command for name '" + name + "'!");
            return;
        }
        commands.put(name, command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        CommandNode found = commands.get(command.getName());
        if (found == null) return false;

        found.onCommand(new NukkitCMDSender(sender), label, args);
        return true;
    }

    protected boolean isNewVersionAvailable(String versionStringUrl) throws IOException {
        return Version.checkVersion(getVersion(), versionStringUrl);
    }

    @Override
    public void reloadPlugin(boolean full) {
        PluginCommon.reload(this, full);
    }

    @Override
    public boolean isReloading() {
        return reloading;
    }

    @Override
    public void setReloading(boolean reloading) {
        this.reloading = reloading;
    }

    public RunnableFactory getRunnableFactory() {
        return runnableFactory;
    }

    @Override
    public PluginLogger getPluginLogger() {
        return logger;
    }

    @Override
    public DebugLogger getDebugLogger() {
        return debugLogger;
    }

    @Override
    @Deprecated
    public ErrorHandler getErrorHandler() {
        return null;
    }

    @Override
    public Timings getTimings() {
        return timings;
    }

    @Override
    public void setDebugLoggers(DebugLogger... loggers) {
        debugLogger.setDebugLoggers(loggers);
    }

    @Override
    public void setErrorHandlers(ErrorHandler... errorHandlers) { }

    @Override
    public void onReload() {
        // No implementation, override to be called on reload.
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }
}