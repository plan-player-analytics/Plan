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

import com.djrapitops.plan.component.Component;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class FabricPluginLogger implements PluginLogger {

    private static final String CHAT_COLOR_REGEX = "§[0-9a-fk-or]";
    private static final String MESSAGE_FORMAT = "[Plan] {}";

    private final Logger logger;

    public FabricPluginLogger(Logger logger) {
        this.logger = logger;
    }

    @NotNull
    private static String removeChatColors(String message) {
        return message.replaceAll(CHAT_COLOR_REGEX, "");
    }

    @Override
    public PluginLogger info(String message) {
        logger.info(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message);
        return this;
    }

    @Override
    public PluginLogger warn(String message) {
        logger.warn(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message);
        return this;
    }

    @Override
    public PluginLogger error(String message) {
        logger.error(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message);
        return this;
    }

    @Override
    public PluginLogger warn(String message, Throwable throwable) {
        logger.warn(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message, throwable);
        return this;
    }

    @Override
    public PluginLogger error(String message, Throwable throwable) {
        logger.error(MESSAGE_FORMAT, StringUtils.contains(message, Component.SECTION) ? removeChatColors(message) : message, throwable);
        return this;
    }
}
