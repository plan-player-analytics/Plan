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
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Task in charge of writing html customized files on enable when they don't exist yet.
 *
 * @author AuroraLS3
 */
@Singleton
public class ResourceWriteTask extends TaskSystem.Task {

    private final PlanConfig config;
    private final PlanFiles files;
    private final ErrorLogger errorLogger;

    @Inject
    public ResourceWriteTask(PlanConfig config, PlanFiles files, ErrorLogger errorLogger) {
        this.config = config;
        this.files = files;
        this.errorLogger = errorLogger;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this).runTaskLaterAsynchronously(3, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        ResourceService resourceService = ResourceService.getInstance();
        Optional<ConfigNode> planCustomizationNode = getPlanCustomizationNode();
        if (planCustomizationNode.isPresent()) {
            for (ConfigNode child : planCustomizationNode.get().getChildren()) {
                if (child.getBoolean()) {
                    String resource = child.getKey(false).replace(',', '.');
                    try {
                        resourceService.getResource("Plan", resource, () -> files.getResourceFromJar("web/" + resource).asWebResource());
                    } catch (UncheckedIOException resourceNoLongerFound) {
                        if (!(resourceNoLongerFound.getCause() instanceof FileNotFoundException)) {
                            errorLogger.error(resourceNoLongerFound, ErrorContext.builder()
                                    .whatToDo("A resource could not be read from the jar: " + resource + ", try restarting the server. Report this if error still occurs afterwards: " + resourceNoLongerFound.getMessage())
                                    .build());
                        }
                    }
                }
            }
        }
    }

    private Optional<ConfigNode> getPlanCustomizationNode() {
        return config.getResourceSettings().getCustomizationConfigNode().getNode("Plan");
    }
}
