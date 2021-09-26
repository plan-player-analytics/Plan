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
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.ResponseBuilder;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.locale.LangCode;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.file.FileResource;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Resolves JSON requests for /v1/locale.
 */
@Singleton
public class LocaleJSONResolver implements NoAuthResolver {

    private final Locale locale;
    private final PlanFiles files;
    private final Addresses addresses;

    @Inject
    public LocaleJSONResolver(
            Locale locale,
            PlanFiles files,
            Addresses addresses
    ) {
        this.locale = locale;
        this.files = files;
        this.addresses = addresses;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        String langCode = request.getQuery().get("lang").orElse("default");
        ResponseBuilder builder = Response.builder();

        try {
            return builder.setJSONContent(getLocaleJSON(langCode)).build();
        } catch (NotFoundException e) {
            String address = addresses.getMainAddress().orElse(addresses.getFallbackLocalhostAddress());
            return builder.setStatus(404)
                    .setJSONContent("{\n" +
                            "  \"message\": \"Language code not found, see " + address + "/v1/language for available codes.\"\n" +
                            "}")
                    .build();
        }
    }

    private Map<String, Object> getLocaleJSON(String langCode) throws NotFoundException {
        Map<String, Object> map = new TreeMap<>();
        Config loadedLocale;

        if ("default".equals(langCode)) {
            LangCode currentLang = locale.getLangCode();

            if (LangCode.CUSTOM.equals(currentLang)) {
                loadedLocale = loadLocale(new FileResource("locale.yml", files.getFileFromPluginFolder("locale.yml")));
            } else {
                loadedLocale = loadLocale(files.getResourceFromJar("locale/" + currentLang.getFileName()));
            }
        } else {
            try {
                LangCode code = LangCode.valueOf(langCode.toUpperCase());
                loadedLocale = loadLocale(files.getResourceFromJar("locale/" + code.getFileName()));
            } catch (IllegalArgumentException e) {
                throw new NotFoundException("Language code not found");
            }
        }
        return dfs(loadedLocale, map);
    }

    private Config loadLocale(Resource resource) {
        try (ConfigReader reader = new ConfigReader(resource.asInputStream())) {
            return reader.read();
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + resource.getResourceName(), e);
        }
    }

    private Map<String, Object> dfs(ConfigNode node, Map<String, Object> parent) {
        for (ConfigNode child : node.getChildren()) {
            if (!child.isLeafNode()) {
                Map<String, Object> childMap = new TreeMap<>();
                parent.put(child.getKey(false), childMap);
                dfs(child, childMap);
            } else {
                parent.put(child.getKey(false), child.getString());
            }
        }
        return parent;
    }
}
