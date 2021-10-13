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
package com.djrapitops.plan;

import com.velocitypowered.api.proxy.ProxyServer;
import io.github.slimjar.injector.loader.Injectable;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

/**
 * Custom {@link Injectable} implementation for Velocity.
 * Appends dependencies to the classpath via Velocity's internal methods.
 */
public class VelocityInjectable implements Injectable {

    private final PlanPlugin plugin;
    private final ProxyServer proxyServer;
    private final PluginLogger logger;

    public VelocityInjectable(PlanPlugin plugin, ProxyServer proxyServer, PluginLogger logger) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
        this.logger = logger;
    }

    @Override
    public void inject(URL url) throws IOException, InvocationTargetException, IllegalAccessException, URISyntaxException {
        logger.info("Proposed " + Paths.get(url.toURI()).getFileName().toString() + " to classpath");
        proxyServer.getPluginManager().addToClasspath(plugin, Paths.get(url.toURI()));
    }
}
