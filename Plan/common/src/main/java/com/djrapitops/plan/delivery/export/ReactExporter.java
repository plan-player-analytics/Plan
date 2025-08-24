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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.delivery.rendering.BundleAddressCorrection;
import com.djrapitops.plan.delivery.web.AssetVersions;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.resolver.json.RootJSONResolver;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.LangCode;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Exporter in charge of exporting React related files.
 *
 * @author AuroraLS3
 */
@Singleton
public class ReactExporter extends FileExporter {

    private final PlanFiles files;
    private final PlanConfig config;
    private final RootJSONResolver jsonHandler;
    private final AssetVersions assetVersions;
    private final BundleAddressCorrection bundleAddressCorrection;

    @Inject
    public ReactExporter(
            PlanFiles files,
            PlanConfig config,
            RootJSONResolver jsonHandler,
            AssetVersions assetVersions,
            BundleAddressCorrection bundleAddressCorrection
    ) {
        this.files = files;
        this.config = config;
        this.jsonHandler = jsonHandler;
        this.assetVersions = assetVersions;
        this.bundleAddressCorrection = bundleAddressCorrection;
    }

    public void exportReactFiles(Path toDirectory) throws IOException {
        exportIndexHtml(toDirectory);
        exportAsset(toDirectory, "favicon.ico");
        exportAsset(toDirectory, "logo192.png");
        exportAsset(toDirectory, "logo512.png");
        exportAsset(toDirectory, "manifest.json");
        exportAsset(toDirectory, "robots.txt");
        exportAsset(toDirectory, "pageExtensionApi.js");
        exportStaticBundle(toDirectory);
        exportLocaleJson(toDirectory.resolve("locale"));
        exportMetadataJson(toDirectory.resolve("metadata"));
        exportThemeJson(toDirectory.resolve("theme"));
        exportReactRedirects(toDirectory, files, config, new String[]{
                "theme-editor",
                "theme-editor/new",
                "theme-editor/delete",
        });
    }

    private void exportThemeJson(Path toDirectory) throws IOException {
        List<String> themeNames = assetVersions.getThemeNames();
        for (String themeName : themeNames) {
            exportJson(toDirectory, "theme?theme=" + themeName, themeName);
        }
    }

    private void exportMetadataJson(Path toDirectory) throws IOException {
        exportJson(toDirectory, "metadata");
        exportJson(toDirectory, "version");
        exportJson(toDirectory, "networkMetadata");
        exportJson(toDirectory, "preferences");
    }

    private void exportLocaleJson(Path toDirectory) throws IOException {
        exportJson(toDirectory, "locale"); // List of languages
        for (LangCode langCode : LangCode.values()) {
            exportJson(toDirectory, "locale/" + langCode.name(), langCode.name());
        }
    }

    private void exportStaticBundle(Path toDirectory) throws IOException {
        deleteOldStaticBundleFiles(toDirectory);
        List<String> paths = assetVersions.getAssetPaths().stream()
                .filter(path -> path.contains("static"))
                .map(path -> path.replace(',', '.'))
                .collect(Collectors.toList());
        for (String path : paths) {
            Path to = toDirectory.resolve(path);
            Resource resource = files.getResourceFromJar("web/" + path);
            // Make static asset loading work with subdirectory addresses
            if (path.endsWith(".css")) {
                String contents = resource.asString();
                String withReplaced = bundleAddressCorrection.correctAddressForExport(contents, path);
                export(to, withReplaced);
            } else if (path.endsWith(".js")) {
                String withReplacedConstants = StringUtils.replaceEach(
                        resource.asString(),
                        new String[]{"PLAN_BASE_ADDRESS", "PLAN_EXPORTED_VERSION"},
                        new String[]{config.get(WebserverSettings.EXTERNAL_LINK), "true"}
                );
                String withReplaced = bundleAddressCorrection.correctAddressForExport(withReplacedConstants, path);
                export(to, withReplaced);
            } else {
                export(to, resource);
            }
        }
    }

    private void deleteOldStaticBundleFiles(Path toDirectory) throws IOException {
        Set<Path> filesToDelete;
        Path staticDirectory = toDirectory.resolve("static");
        if (!Files.isDirectory(staticDirectory)) return;
        try (Stream<Path> exportedFiles = Files.walk(staticDirectory)) {
            filesToDelete = exportedFiles
                    .filter(path -> {
                        String filePath = path.toFile().getPath();
                        return filePath.contains(".chunk") || filePath.contains("main.");
                    })
                    .collect(Collectors.toSet());
        }
        for (Path path : filesToDelete) {
            Files.deleteIfExists(path);
        }
    }

    private void exportIndexHtml(Path toDirectory) throws IOException {
        String contents = files.getResourceFromJar("web/index.html")
                .asString();
        contents = bundleAddressCorrection.correctAddressForExport(contents, "index.html");

        export(toDirectory.resolve("index.html"), contents);
    }

    private void exportAsset(Path toDirectory, String asset) throws IOException {
        export(toDirectory.resolve(asset), files.getResourceFromJar("web/" + asset));
    }

    private void exportJson(Path toDirectory, String resource) throws IOException {
        exportJson(toDirectory, resource, toJsonResourceName(resource));
    }

    private void exportJson(Path toDirectory, String resource, String fileName) throws IOException {
        Path to = toDirectory.resolve(fileName + ".json");
        Optional<Response> jsonResponse = getJsonResponse(resource);
        if (jsonResponse.isPresent()) {
            export(to, jsonResponse.get().getBytes());
        }
    }

    private String toJsonResourceName(String resource) {
        return StringUtils.replaceEach(resource, new String[]{"?", "&",}, new String[]{"-", "_"});
    }

    private Optional<Response> getJsonResponse(String resource) {
        try {
            return jsonHandler.getResolver().resolve(new Request("GET", "/v1/" + resource, null, Collections.emptyMap(), null));
        } catch (WebUserAuthException e) {
            // The rest of the exceptions should not be thrown
            throw new IllegalStateException("Unexpected exception thrown: " + e, e);
        }
    }

}
