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
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.locale.LangCode;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Resolves JSON requests for /v1/locale and /v1/locale/{@link LangCode#toString()}.
 *
 * @author Kopo942
 */
@Singleton
public class LocaleJSONResolver implements NoAuthResolver {

    private final LocaleSystem localeSystem;
    private final Locale locale;
    private final PlanFiles files;
    private final Addresses addresses;

    @Inject
    public LocaleJSONResolver(
            LocaleSystem localeSystem,
            Locale locale,
            PlanFiles files,
            Addresses addresses
    ) {
        this.localeSystem = localeSystem;
        this.locale = locale;
        this.files = files;
        this.addresses = addresses;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        ResponseBuilder builder = Response.builder();

        String path = request.omitFirstInPath().getPath().asString().replaceAll("^/|/$", "");
        Map<String, Object> json = "".equals(path) ? getLanguageListJSON() : getLocaleJSON(path);

        if (!json.isEmpty()) {
            return builder.setJSONContent(json).build();
        } else {
            String address = addresses.getMainAddress().orElse(addresses.getFallbackLocalhostAddress());
            return builder.setStatus(404)
                    .setJSONContent("{\n" +
                            "  \"message\": \"Language not found, see " + address +
                            "/v1/locale for available language codes.\"\n" +
                            "}")
                    .build();
        }
    }

    private Map<String, Object> getLanguageListJSON() {
        Map<String, Object> json = new HashMap<>();
        Map<String, Object> languages = new TreeMap<>();
        Map<String, Object> languageVersions = new TreeMap<>();

        long localeVersion = localeSystem.getLocaleVersion();
        Optional<Long> customLocaleVersion = localeSystem.getCustomLocaleVersion();

        for (LangCode lang : LangCode.values()) {
            if (lang == LangCode.CUSTOM && locale.getLangCode() != LangCode.CUSTOM) continue;
            languages.put(lang.toString(), lang.getName());
            languageVersions.put(lang.toString(), localeVersion);
        }
        customLocaleVersion.ifPresent(version -> languageVersions.put(LangCode.CUSTOM.toString(), version));

        json.put("defaultLanguage", locale.getLangCode().toString());
        json.put("languages", languages);
        json.put("languageVersions", languageVersions);

        return json;
    }

    private Map<String, Object> getLocaleJSON(String langCode) {
        LangCode code = LangCode.valueOf(langCode.toUpperCase());
        Map<String, Object> json = new TreeMap<>();
        Resource file;

        if (code == LangCode.CUSTOM) {
            if (locale.getLangCode() != LangCode.CUSTOM || !files.getFileFromPluginFolder("locale.yml").exists()) {
                return json;
            }
            file = files.getResourceFromPluginFolder("locale.yml");
        } else {
            file = files.getResourceFromJar("locale/" + code.getFileName());
        }

        try {
            return dfs(loadLocale(file), json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Config loadLocale(Resource resource) throws IOException {
        try (ConfigReader reader = new ConfigReader(resource.asInputStream())) {
            return reader.read();
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
