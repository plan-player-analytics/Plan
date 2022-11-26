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

import com.djrapitops.plan.delivery.web.AssetVersions;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.Resource;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exporter in charge of exporting React related files.
 *
 * @author AuroraLS3
 */
@Singleton
public class ReactExporter extends FileExporter {

    private final PlanFiles files;
    private final PlanConfig config;
    private final AssetVersions assetVersions;

    @Inject
    public ReactExporter(
            PlanFiles files,
            PlanConfig config,
            AssetVersions assetVersions
    ) {
        this.files = files;
        this.config = config;
        this.assetVersions = assetVersions;
    }

    public void exportReactFiles(Path toDirectory) throws IOException {
        exportAsset(toDirectory, "index.html");
        exportAsset(toDirectory, "asset-manifest.json");
        exportAsset(toDirectory, "favicon.ico");
        exportAsset(toDirectory, "logo192.png");
        exportAsset(toDirectory, "logo512.png");
        exportAsset(toDirectory, "manifest.json");
        exportAsset(toDirectory, "robots.txt");
        exportStaticBundle(toDirectory);
    }

    private void exportStaticBundle(Path toDirectory) throws IOException {
        List<String> paths = assetVersions.getAssetPaths().stream()
                .filter(path -> path.contains("static"))
                .collect(Collectors.toList());
        for (String path : paths) {
            String resourcePath = path.replace(',', '.');
            Path to = toDirectory.resolve(resourcePath);
            Resource resource = files.getResourceFromJar("web/" + resourcePath);
            if (resourcePath.endsWith(".js")) {
                String withReplacedConstants = StringUtils.replaceEach(
                        resource.asString(),
                        new String[]{"PLAN_BASE_ADDRESS", "PLAN_EXPORTED_VERSION"},
                        new String[]{config.get(WebserverSettings.EXTERNAL_LINK), "true"}
                );
                export(to, withReplacedConstants);
            } else {
                export(to, resource);
            }
        }
    }

    private void exportAsset(Path toDirectory, String asset) throws IOException {
        export(toDirectory.resolve(asset), files.getResourceFromJar("web/" + asset));
    }

}
