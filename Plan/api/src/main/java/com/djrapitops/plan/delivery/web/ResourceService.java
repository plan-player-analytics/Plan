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

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resource.WebResource;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Service for making plugin resources customizable by user or Plan API.
 *
 * @author AuroraLS3
 */
public interface ResourceService {

    static ResourceService getInstance() {
        return Optional.ofNullable(ResourceService.Holder.service.get())
                .orElseThrow(() -> new IllegalStateException("ResourceService has not been initialised yet."));
    }

    /**
     * Make one of your web resources customizable by user or Plan API.
     *
     * @param pluginName Name of your plugin (for config purposes)
     * @param fileName   Name of the file (for customization)
     * @param source     Supplier to use to get the original resource, it is assumed that any text based files are encoded in UTF-8.
     * @return Resource of the customized file.
     * @throws IllegalArgumentException If pluginName is empty or null
     * @throws IllegalArgumentException If fileName is empty or null
     * @throws IllegalArgumentException If source is null
     */
    WebResource getResource(String pluginName, String fileName, Supplier<WebResource> source);

    /**
     * Add javascript to load in a html resource.
     * <p>
     * Adds {@code <script src="jsSrc"></script>} or multiple to the resource.
     *
     * @param pluginName Name of your plugin (for config purposes)
     * @param fileName   Name of the .html file being modified
     * @param position   Where to place the script tag on the page.
     * @param jsSources  Source URLs.
     * @throws IllegalArgumentException If pluginName is empty or null
     * @throws IllegalArgumentException If fileName is null, empty or does not end with .html
     * @throws IllegalArgumentException If position null
     * @throws IllegalArgumentException If jsSources is empty or null
     */
    void addScriptsToResource(String pluginName, String fileName, Position position, String... jsSources);

    /**
     * Add javascript to load in a html resource.
     * <p>
     * Requires PAGE_EXTENSION_RESOURCES_REGISTER_DIRECT_CUSTOMIZATION Capability.
     *
     * @param pluginName         Name of your plugin (for config purposes)
     * @param fileName           Name of the .html file being modified
     * @param position           Where to place the script tag on the page.
     * @param scriptName         Name of your javascript file (used on the page)
     * @param javascriptAsString Javascript file contents in UTF-8
     * @throws IllegalArgumentException If fileName is null, empty or does not end with .html
     * @throws IllegalArgumentException If anything is empty or null
     */
    default void addJavascriptToResource(String pluginName, String fileName, Position position, String scriptName, String javascriptAsString) {
        if (javascriptAsString == null || javascriptAsString.isEmpty()) {
            throw new IllegalArgumentException("null or empty 'javascriptAsString' given.");
        }
        if (scriptName == null || scriptName.isEmpty()) {
            throw new IllegalArgumentException("null or empty 'scriptName' given.");
        }
        String actualScriptName = scriptName.endsWith(".js") ? scriptName : scriptName + ".js";

        addScriptsToResource(pluginName, fileName, position, "/" + pluginName + "/" + actualScriptName);
        ResolverService.getInstance().registerResolver(pluginName, "/" + pluginName + "/" + actualScriptName, (NoAuthResolver) request -> {
            if (request.getPath().asString().endsWith(actualScriptName)) {
                return Optional.of(Response.builder()
                        .setContent(javascriptAsString)
                        .setMimeType(MimeType.JS)
                        .build());
            }
            return Optional.empty();
        });
    }

    /**
     * Add css to load in an existing html resource.
     * <p>
     * Adds {@code <link href="cssSrc" rel="stylesheet"></link>} or multiple to the resource.
     *
     * @param pluginName Name of your plugin (for config purposes)
     * @param fileName   Name of the .html file being modified
     * @param position   Where to place the link tag on the page.
     * @param cssSources Source URLs.
     * @throws IllegalArgumentException If pluginName is empty or null
     * @throws IllegalArgumentException If fileName is null, empty or does not end with .html
     * @throws IllegalArgumentException If position null
     * @throws IllegalArgumentException If cssSources is empty or null
     */
    void addStylesToResource(String pluginName, String fileName, Position position, String... cssSources);


    /**
     * Add javascript to load in a html resource.
     * <p>
     * Requires PAGE_EXTENSION_RESOURCES_REGISTER_DIRECT_CUSTOMIZATION Capability.
     *
     * @param pluginName  Name of your plugin (for config purposes)
     * @param fileName    Name of the .html file being modified
     * @param position    Where to place the script tag on the page.
     * @param cssFileName Name of your css file (used on the page)
     * @param cssAsString CSS file contents in UTF-8
     * @throws IllegalArgumentException If fileName is null, empty or does not end with .html
     * @throws IllegalArgumentException If anything is empty or null
     */
    default void addStyleToResource(String pluginName, String fileName, Position position, String cssFileName, String cssAsString) {
        if (cssAsString == null || cssAsString.isEmpty()) {
            throw new IllegalArgumentException("null or empty 'cssAsString' given.");
        }
        if (cssFileName == null || cssFileName.isEmpty()) {
            throw new IllegalArgumentException("null or empty 'cssFileName' given.");
        }
        String actualCssFileName = cssFileName.endsWith(".js") ? cssFileName : cssFileName + ".js";

        addStylesToResource(pluginName, fileName, position, pluginName + "/" + actualCssFileName);
        ResolverService.getInstance().registerResolver(pluginName, pluginName + "/" + actualCssFileName, (NoAuthResolver) request -> {
            if (request.getPath().asString().equals(actualCssFileName)) {
                return Optional.of(Response.builder()
                        .setContent(cssAsString)
                        .setMimeType(MimeType.CSS)
                        .build());
            }
            return Optional.empty();
        });
    }

    enum Position {
        /**
         * Loaded before page contents.
         * <p>
         * Recommended for loading style sheets.
         */
        PRE_CONTENT,
        /**
         * Loaded after library scripts.
         * <p>
         * Recommended for modifying the structure of the page or loading libraries.
         */
        PRE_MAIN_SCRIPT,
        /**
         * Loaded after script execution.
         * <p>
         * Recommended for loading data to custom structure on the page.
         *
         * @see <a href="https://github.com/plan-player-analytics/Plan/blob/master/Plan/react/dashboard/public/pageExtensionApi.js">Javascript API</a>
         * @deprecated No longer supported on React pages, use the javascript API in PRE_MAIN_SCRIPT.
         */
        @Deprecated
        AFTER_MAIN_SCRIPT;

        public String cleanName() {
            return name().toLowerCase().replace('_', ' ');
        }
    }

    class Holder {
        static final AtomicReference<ResourceService> service = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(ResourceService service) {
            ResourceService.Holder.service.set(service);
        }
    }
}
