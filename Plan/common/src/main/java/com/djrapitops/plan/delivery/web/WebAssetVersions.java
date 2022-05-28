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

import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.storage.file.PlanFiles;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.Optional;

@Singleton
public class WebAssetVersions {

    private final PlanFiles files;
    private Config webAssetConfig;

    @Inject
    public WebAssetVersions(
            PlanFiles files
    ) {
        this.files = files;
    }

    public void prepare() throws IOException {
        try (ConfigReader reader = new ConfigReader(files.getResourceFromJar("WebAssetVersion.yml").asInputStream())) {
            webAssetConfig = reader.read();
        }
    }

    public Optional<Long> getWebAssetVersion(String resource) {
        if (webAssetConfig == null) return Optional.empty();

        return webAssetConfig.getNode(resource.replace('.', ',')).map(ConfigNode::getLong);
    }

    public Optional<Long> getLatestWebAssetVersion() {
        if (webAssetConfig == null) return Optional.empty();

        long max = 0;
        for (String configPath : webAssetConfig.getConfigPaths()) {
            max = Math.max(max, webAssetConfig.getLong(configPath));
        }

        return Optional.of(max);
    }
}
