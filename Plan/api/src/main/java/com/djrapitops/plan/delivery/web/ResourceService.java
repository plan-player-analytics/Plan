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
package com.djrapitops.plan.delivery.web;

import com.djrapitops.plan.delivery.web.resource.Resource;

import java.util.function.Supplier;

/**
 * Service for making plugin resources customizable by user or Plan API.
 *
 * @author Rsl1122
 */
public interface ResourceService {

    /**
     * Make one of your web resources customizable by user or Plan API.
     *
     * @param pluginName Name of your plugin (for config purposes)
     * @param fileName   Name of the file (for customization)
     * @param source     Supplier to use to get the original resource.
     * @return Resource of the customized file.
     */
    Resource getResource(String pluginName, String fileName, Supplier<Resource> source);

    /**
     * Add javascript to load in an existing html resource.
     * <p>
     * Adds {@code <script src="jsSrc"></script>} or multiple to the resource.
     *
     * @param pluginName Name of your plugin (for config purposes)
     * @param fileName   Name of the .html file being modified
     * @param position   Where to place the script tag on the page.
     * @param jsSrcs     Source URLs.
     * @throws IllegalArgumentException If fileName does not end with .html or .htm
     */
    void addScriptsToResource(String pluginName, String fileName, Position position, String... jsSrcs);

    /**
     * Add css to load in an existing html resource.
     * <p>
     * Adds {@code <link href="cssSrc" rel="stylesheet"></link>} or multiple to the resource.
     *
     * @param pluginName Name of your plugin (for config purposes)
     * @param fileName   Name of the .html file being modified
     * @param position   Where to place the link tag on the page.
     * @param cssSrcs    Source URLs.
     * @throws IllegalArgumentException If fileName does not end with .html or .htm
     */
    void addStylesToResource(String pluginName, String fileName, Position position, String... cssSrcs);

    enum Position {
        /**
         * Loaded before page contents.
         */
        HEAD,
        /**
         * Loaded after page contents.
         * <p>
         * Recommended for modifying the structure of the page or loading libraries.
         */
        BODY,
        /**
         * Loaded after script execution.
         * <p>
         * Recommended for loading data to custom structure on the page.
         */
        BODY_END
    }
}
