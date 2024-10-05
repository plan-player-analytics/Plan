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
package com.djrapitops.plan.extension.implementation.providers.gathering;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.exceptions.DataExtensionMethodCallException;
import com.djrapitops.plan.extension.annotation.GraphPointProvider;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.graph.DataPoint;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerGraphPoint;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

/**
 * @author AuroraLS3
 */
public class ServerGraphSampler extends TaskSystem.Task {
    private final ExtensionWrapper extension;
    private final DBSystem dbSystem;
    private final ExtensionMethod provider;
    private final GraphPointProvider annotation;
    private final Parameters parameters;
    private final ProviderIdentifier providerIdentifier;
    private final ErrorLogger errorLogger;

    public ServerGraphSampler(
            ExtensionWrapper extension,
            ExtensionMethod provider,
            DBSystem dbSystem,
            Parameters parameters,
            ProviderIdentifier providerIdentifier,
            ErrorLogger errorLogger
    ) {
        this.extension = extension;
        this.provider = provider;
        annotation = provider.getExistingAnnotation(GraphPointProvider.class);
        this.dbSystem = dbSystem;
        this.parameters = parameters;
        this.providerIdentifier = providerIdentifier;
        this.errorLogger = errorLogger;
    }

    private DataPoint callMethod() {
        return new MethodWrapper<>(provider.getMethod(), DataPoint.class)
                .callMethod(extension.getExtension(), parameters);
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this)
                .runTaskTimerAsynchronously(0, annotation.sampleInterval(), annotation.sampleIntervalUnit());
    }

    @Override
    public void run() {
        try {
            DataPoint dataPoint = callMethod();
            if (dataPoint == null) return;
            dbSystem.getDatabase().executeTransaction(new StoreServerGraphPoint(dataPoint, providerIdentifier));
        } catch (DataExtensionMethodCallException e) {
            errorLogger.warn(e, ErrorContext.builder()
                    .related(providerIdentifier)
                    .whatToDo("Graph sampler for " + providerIdentifier.getPluginName() + "." + providerIdentifier.getProviderName() + " ran into error and was disabled. You can disable the plugin from Plan config and report this.")
                    .build());
            cancel();
        }
    }
}
