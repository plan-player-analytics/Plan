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
package net.playeranalytics.plugin.server;

import org.apache.logging.log4j.Logger;

public class FabricPluginLogger implements PluginLogger {

    private final Logger logger;

    public FabricPluginLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public PluginLogger info(String message) {
        logger.info("[Plan] " + message);
        return this;
    }

    public void info(String message, Object... args) {
        String replacedMsg = message.replaceAll("(?<=\\{).+?(?=\\})", "");
        String formattedMsg = "[Plan] " + replacedMsg;
        logger.info(formattedMsg, args);
    }

    @Override
    public PluginLogger warn(String message) {
        logger.warn("[Plan] " + message);
        return this;
    }

    @Override
    public PluginLogger error(String message) {
        logger.error("[Plan] " + message);
        return this;
    }

    @Override
    public PluginLogger warn(String message, Throwable throwable) {
        logger.warn("[Plan] " + message, throwable);
        return this;
    }

    @Override
    public PluginLogger error(String message, Throwable throwable) {
        logger.error("[Plan] " + message, throwable);
        return this;
    }
}
