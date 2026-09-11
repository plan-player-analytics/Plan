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
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.GraphProvider;
import com.djrapitops.plan.extension.graph.ServerGraphDataSource;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerGraphPoint;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * @author AuroraLS3
 */
public class ServerGraphSampler extends TaskSystem.Task {
    private final DBSystem dbSystem;
    private final ServerGraphDataSource dataSource;
    private final GraphProvider annotation;
    private final ProviderIdentifier providerIdentifier;
    private final ErrorLogger errorLogger;

    public ServerGraphSampler(
            ServerGraphDataSource dataSource,
            GraphProvider annotation,
            DBSystem dbSystem,
            ProviderIdentifier providerIdentifier,
            ErrorLogger errorLogger
    ) {
        this.dataSource = dataSource;
        this.annotation = annotation;
        this.dbSystem = dbSystem;
        this.providerIdentifier = providerIdentifier;
        this.errorLogger = errorLogger;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        Duration minimumInterval = Duration.of(5, TimeUnit.SECONDS.toChronoUnit());
        Duration interval = Duration.of(annotation.sampleInterval(), annotation.sampleIntervalUnit().toChronoUnit());

        int randomDelay = ThreadLocalRandom.current().nextInt(15);
        if (interval.minus(minimumInterval).isNegative()) {
            runnableFactory.create(this)
                    .runTaskTimerAsynchronously(randomDelay, 5, TimeUnit.SECONDS);
        } else {
            runnableFactory.create(this)
                    .runTaskTimerAsynchronously(randomDelay, annotation.sampleInterval(), annotation.sampleIntervalUnit());
        }
    }

    @Override
    public void run() {
        try {
            storePoint();
        } catch (DataExtensionMethodCallException e) {
            errorLogger.warn(e, ErrorContext.builder()
                    .related(providerIdentifier)
                    .whatToDo("Graph sampler for " + providerIdentifier.getPluginName() + "." + providerIdentifier.getProviderName() + " ran into error and was disabled.")
                    .build());
        }
    }

    private void storePoint() {
        try {
            var dataPoint = dataSource.getPoint(System.currentTimeMillis());
            dataPoint.ifPresent(point -> dbSystem.getDatabase().executeTransaction(
                    new StoreServerGraphPoint(point, providerIdentifier)));
        } catch (NotReadyException | UnsupportedOperationException ignored) {
            // Data or API not available to make the call, no-op.
        } catch (Exception | IllegalAccessError | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            throw new DataExtensionMethodCallException("", e, providerIdentifier.getPluginName(), providerIdentifier.getProviderName());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ServerGraphSampler that = (ServerGraphSampler) o;
        return Objects.equals(providerIdentifier, that.providerIdentifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerIdentifier);
    }
}
