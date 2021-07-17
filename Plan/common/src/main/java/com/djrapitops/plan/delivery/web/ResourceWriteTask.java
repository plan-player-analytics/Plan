package com.djrapitops.plan.delivery.web;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.settings.config.ConfigNode;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.file.PlanFiles;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
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

    @Inject
    public ResourceWriteTask(PlanConfig config, PlanFiles files) {
        this.config = config;
        this.files = files;
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
                    resourceService.getResource("Plan", resource, () -> files.getResourceFromJar("web/" + resource).asWebResource());
                }
            }
        }
    }

    private Optional<ConfigNode> getPlanCustomizationNode() {
        return config.getResourceSettings().getCustomizationConfigNode().getNode("Plan");
    }
}
