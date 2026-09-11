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
import com.djrapitops.plan.extension.annotation.GraphProvider;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.graph.PlayerGraphDataSource;
import com.djrapitops.plan.extension.implementation.ExtensionMethodErrorTracker;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerGraphPoint;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Samples graph data for a player at an interval defined by the plugin.
 *
 * @author AuroraLS3
 */
public class PlayerGraphSampler extends TaskSystem.Task {

    private final ExtensionWrapper extension;
    private final DBSystem dbSystem;
    private final PlayerGraphDataSource dataSource;
    private final ExtensionMethod provider;
    private final GraphProvider annotation;
    private final Parameters.PlayerParameters parameters;
    private final ProviderIdentifier providerIdentifier;
    private final ErrorLogger errorLogger;

    public PlayerGraphSampler(ExtensionWrapper extension, PlayerGraphDataSource dataSource, ExtensionMethod provider, DBSystem dbSystem, Parameters.PlayerParameters parameters, ProviderIdentifier providerIdentifier, ErrorLogger errorLogger) {
        this.extension = extension;
        this.dataSource = dataSource;
        this.provider = provider;
        this.annotation = provider.getExistingAnnotation(GraphProvider.class);
        this.dbSystem = dbSystem;
        this.parameters = parameters;
        this.providerIdentifier = providerIdentifier;
        this.errorLogger = errorLogger;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        if (ExtensionMethodErrorTracker.isDisabled(extension, provider)) return;

        Duration minimumInterval = Duration.of(30, TimeUnit.SECONDS.toChronoUnit());
        Duration interval = Duration.of(annotation.sampleInterval(), annotation.sampleIntervalUnit().toChronoUnit());

        int randomDelay = ThreadLocalRandom.current().nextInt(15);
        if (interval.minus(minimumInterval).isNegative()) {
            runnableFactory.create(this)
                    .runTaskTimerAsynchronously(randomDelay, 30, TimeUnit.SECONDS);
        } else {
            runnableFactory.create(this)
                    .runTaskTimerAsynchronously(randomDelay, annotation.sampleInterval(), annotation.sampleIntervalUnit());
        }
    }

    public void unregister() {
        cancel();
    }

    @Override
    public void run() {
        try {
            var dataPoint = dataSource.getPoint(System.currentTimeMillis(), parameters.getPlayerUUID(), parameters.getPlayerName());
            dataPoint.ifPresent(point -> dbSystem.getDatabase().executeTransaction(
                    new StorePlayerGraphPoint(point, parameters.getPlayerUUID(), providerIdentifier)));
        } catch (DataExtensionMethodCallException e) {
            errorLogger.warn(e, ErrorContext.builder()
                    .related(providerIdentifier)
                    .whatToDo("Player Graph sampler for " + providerIdentifier.getPluginName() + "." + providerIdentifier.getProviderName() + " ran into error and was disabled. You can disable the plugin from Plan config and report this.")
                    .build());
            ExtensionMethodErrorTracker.errored(extension, provider);
            cancel();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PlayerGraphSampler that = (PlayerGraphSampler) o;
        return Objects.equals(providerIdentifier, that.providerIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerIdentifier);
    }
}
