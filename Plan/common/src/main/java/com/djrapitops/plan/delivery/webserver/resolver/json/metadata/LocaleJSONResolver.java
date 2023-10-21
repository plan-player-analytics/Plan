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
package com.djrapitops.plan.delivery.webserver.resolver.json.metadata;

import com.djrapitops.plan.delivery.web.resolver.MimeType;
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
import com.djrapitops.plan.settings.locale.lang.Lang;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import com.djrapitops.plan.utilities.dev.Untrusted;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;

/**
 * Resolves JSON requests for /v1/locale and /v1/locale/{@link LangCode#toString()}.
 *
 * @author Kopo942
 */
@Singleton
@Path("/v1/locale/{langCode}")
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

    @GET
    @Operation(
            responses = {
                    @ApiResponse(responseCode = "200 (/locale)", description = "List of available locales", content = @Content(mediaType = MimeType.JSON, examples = {
                            @ExampleObject("{\"defaultLanguage\": \"EN\", \"languages\": {\"EN\": \"English\", \"FI\": \"Finnish\"}, \"languageVersions\": {\"EN\": 1657189514266, \"FI\": 1657189514266}}")
                    })),
                    @ApiResponse(responseCode = "200 (/locale/{langCode})", description = "Contents of the locale.json file matching given langCode"),
                    @ApiResponse(responseCode = "404", description = "Language by langCode was not found")
            },
            parameters = {
                    @Parameter(in = ParameterIn.PATH, name = "langCode", description = "Language code. NOT REQUIRED. /v1/locale lists available language codes.", allowEmptyValue = true, example = "/v1/locale/EN")
            },
            requestBody = @RequestBody(content = @Content(examples = @ExampleObject()))
    )
    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    private Response getResponse(Request request) {
        ResponseBuilder builder = Response.builder();

        @Untrusted Optional<String> langCode = request.getPath().getPart(1);
        Map<String, Object> json = langCode
                .map(this::getLocaleJSON)
                .orElseGet(this::getLanguageListJSON);

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

        long maxLocaleVersion = localeSystem.getMaxLocaleVersion();
        Optional<Long> customLocaleVersion = localeSystem.getCustomLocaleVersion();

        for (LangCode lang : LangCode.values()) {
            if (lang == LangCode.CUSTOM && locale.getLangCode() != LangCode.CUSTOM) continue;
            languages.put(lang.toString(), lang.getName());
            long localeVersion = localeSystem.getLocaleVersion(lang).orElse(maxLocaleVersion);
            languageVersions.put(lang.toString(), localeVersion);
        }
        customLocaleVersion.ifPresent(version -> languageVersions.put(LangCode.CUSTOM.toString(), version));

        json.put("defaultLanguage", locale.getLangCode().toString());
        json.put("languages", languages);
        json.put("languageVersions", languageVersions);

        return json;
    }

    private Map<String, Object> getLocaleJSON(@Untrusted String langCode) {
        try {
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

            return dfs(loadLocale(file), json);
        } catch (@Untrusted IllegalArgumentException noSuchEnum) {
            return Collections.emptyMap();
        } catch (IOException dfsFileLookupError) {
            throw new UncheckedIOException(dfsFileLookupError);
        }
    }

    private Config loadLocale(Resource resource) throws IOException {
        try (ConfigReader reader = new ConfigReader(resource.asInputStream())) {
            Config config = reader.read();
            addMissingKeys(config);
            return config;
        }
    }

    private void addMissingKeys(Config config) {
        for (Map.Entry<String, Lang> entry : LocaleSystem.getKeys().entrySet()) {
            String key = entry.getKey();
            if (config.contains(key)) continue;
            config.set(key, locale.getString(entry.getValue()));
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
