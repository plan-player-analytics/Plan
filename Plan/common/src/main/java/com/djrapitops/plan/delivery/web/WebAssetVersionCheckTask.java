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

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.file.PlanFiles;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Task in charge of checking html customized files on enable to see if they are outdated.
 */
@Singleton
public class WebAssetVersionCheckTask extends TaskSystem.Task {

    private final PlanConfig config;
    private final PlanFiles files;
    private final PluginLogger logger;
    private final WebAssetVersions webAssetVersions;
    private final Formatters formatters;

    @Inject
    public WebAssetVersionCheckTask(
            PlanConfig config,
            PlanFiles files,
            PluginLogger logger,
            WebAssetVersions webAssetVersions,
            Formatters formatters
    ) {
        this.config = config;
        this.files = files;
        this.logger = logger;
        this.webAssetVersions = webAssetVersions;
        this.formatters = formatters;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this).runTaskLaterAsynchronously(3, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Optional<ConfigNode> planCustomizationNode = getPlanCustomizationNode();
        if (planCustomizationNode.isPresent()) {
            try {
                webAssetVersions.prepare();
            } catch (IOException e) {
                logger.warn(String.format("Could not read web asset versions, %s", e.toString()));
                logger.warn("Web asset version check will be skipped!");
                return;
            }

            List<AssetInfo> outdated = new ArrayList<>();

            for (ConfigNode child : planCustomizationNode.get().getChildren()) {
                if (child.getBoolean()) {
                    String resource = child.getKey(false).replace(',', '.');
                    findOutdatedResource(resource).ifPresent(outdated::add);
                }
            }

            if (!outdated.isEmpty()) {
                logger.warn("You have customized files which are out of date due to recent updates!");
                logger.warn("Plan may not work properly until these files are updated to include the new modifications.");
                logger.warn("See https://github.com/plan-player-analytics/Plan/commits/html to compare changes");
            }
            for (AssetInfo asset : outdated) {
                logger.warn(String.format("- %s was modified %s, but the plugin contains a version from %s",
                        asset.filename,
                        formatters.secondLong().apply(asset.modifiedAt),
                        formatters.secondLong().apply(asset.expectedModifiedAt)
                ));
            }
        }
    }

    private Optional<AssetInfo> findOutdatedResource(String resource) {
        Optional<File> resourceFile = files.attemptToFind(resource);
        Optional<Long> webAssetVersion = webAssetVersions.getWebAssetVersion(resource);
        if (resourceFile.isPresent() && webAssetVersion.isPresent()) {
            if (webAssetVersion.get() > resourceFile.get().lastModified()) {
                return Optional.of(new AssetInfo(
                        resource,
                        resourceFile.get().lastModified(),
                        webAssetVersion.get()
                ));
            }
        }
        return Optional.empty();
    }

    private Optional<ConfigNode> getPlanCustomizationNode() {
        return config.getResourceSettings().getCustomizationConfigNode().getNode("Plan");
    }

    private static class AssetInfo {
        public String filename;
        public long modifiedAt;
        public long expectedModifiedAt;

        public AssetInfo(String filename, long modifiedAt, long expectedModifiedAt) {
            this.filename = filename;
            this.modifiedAt = modifiedAt;
            this.expectedModifiedAt = expectedModifiedAt;
        }
    }
}
