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
package net.playeranalytics.plan;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.djrapitops.plan.commands.use.Subcommand;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.playeranalytics.plugin.FabricPlatformLayer;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.File;
import java.io.InputStream;

/**
 * Main class for Plan's Fabric version.
 *
 * @author Kopo942
 */
public class PlanFabric implements PlanPlugin, DedicatedServerModInitializer {

    private MinecraftServer server;
    private PluginLogger pluginLogger;
    private RunnableFactory runnableFactory;
    private PlatformAbstractionLayer abstractionLayer;

    @Override
    public InputStream getResource(String resource) {
        return null;
    }

    @Override
    public ColorScheme getColorScheme() {
        return null;
    }

    @Override
    public PlanSystem getSystem() {
        return null;
    }

    @Override
    public void registerCommand(Subcommand command) {

    }

    @Override
    public void onEnable() {
        PlanFabricComponent component = DaggerPlanFabricComponent.builder()
                .plan(this)
                .abstractionLayer(abstractionLayer)
                .server(server)
                .build();
    }

    @Override
    public void onDisable() {

    }

    @Override
    public File getDataFolder() {
        return null;
    }

    @Override
    public void onInitializeServer() {
        // TODO move to separate class?
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            this.server = server;
        });

        abstractionLayer = new FabricPlatformLayer(this);
        pluginLogger = abstractionLayer.getPluginLogger();
        runnableFactory = abstractionLayer.getRunnableFactory();
        onEnable();
    }

    public MinecraftServer getServer() {
        return server;
    }
}
