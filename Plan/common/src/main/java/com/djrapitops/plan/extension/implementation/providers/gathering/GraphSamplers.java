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

import com.djrapitops.plan.exceptions.DataExtensionMethodCallException;
import com.djrapitops.plan.extension.annotation.GraphProvider;
import com.djrapitops.plan.extension.annotation.Tab;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.extractor.ExtensionMethods;
import com.djrapitops.plan.extension.graph.PlayerGraphDataSource;
import com.djrapitops.plan.extension.graph.SeriesMetadata;
import com.djrapitops.plan.extension.graph.ServerGraphDataSource;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.builder.ExtValueBuilder;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreGraphPointProviderTransaction;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This is a utility class for managing extension graph sampling.
 *
 * @author AuroraLS3
 */
@Singleton
public class GraphSamplers {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final RunnableFactory runnableFactory;
    private final ErrorLogger errorLogger;

    private final List<Pair<ExtensionMethod, PlayerGraphDataSource>> playerGraphSources = new ArrayList<>();
    private final Map<UUID, Set<PlayerGraphSampler>> activePlayerGraphSamplers = new ConcurrentHashMap<>();

    @Inject
    public GraphSamplers(ServerInfo serverInfo, DBSystem dbSystem, RunnableFactory runnableFactory, ErrorLogger errorLogger) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;
    }

    public void registerGraphSamplers(ExtensionWrapper extension) {
        Map<ExtensionMethod.ParameterType, ExtensionMethods> methods = extension.getMethods();
        ExtensionMethods extensionMethods = methods.get(ExtensionMethod.ParameterType.SERVER_NONE);

        List<ExtensionMethod> graphPointProviders = extensionMethods.getGraphPointProviders();
        var byReturnType = graphPointProviders.stream()
                .collect(Collectors.groupingBy(ExtensionMethod::getReturnType));
        // TODO History data storage
        registerServerProvidersAndSamplers(extension, byReturnType.get(ServerGraphSampler.class));
        registerPlayerProviders(extension, byReturnType.get(PlayerGraphSampler.class));
    }

    private void registerPlayerProviders(ExtensionWrapper extension, List<ExtensionMethod> graphPointProviders) {
        if (graphPointProviders == null || graphPointProviders.isEmpty()) return;
        Parameters parameters = Parameters.server(serverInfo.getServerUUID());
        for (ExtensionMethod provider : graphPointProviders) {
            ProviderIdentifier providerIdentifier = new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), provider.getMethodName());
            try {
                var playerGraphDataSource = new MethodWrapper<>(provider.getMethod(), PlayerGraphDataSource.class)
                        .callMethod(extension.getExtension(), parameters);
                if (playerGraphDataSource == null) continue;
                storeGraphMetadata(
                        extension,
                        provider,
                        playerGraphDataSource.getSeriesMetadata(),
                        ExtensionGraphMetadataTable.TableType.PLAYER
                );
                playerGraphSources.add(ImmutablePair.of(provider, playerGraphDataSource));
            } catch (DataExtensionMethodCallException e) {
                errorLogger.warn(e, ErrorContext.builder()
                        .related(providerIdentifier)
                        .whatToDo("Graph sampler for " + providerIdentifier.getPluginName() + "." + providerIdentifier.getProviderName() + " ran into error and was not registered.")
                        .build());
            }
        }
    }

    private void registerServerProvidersAndSamplers(ExtensionWrapper extension, List<ExtensionMethod> graphPointProviders) {
        if (graphPointProviders == null || graphPointProviders.isEmpty()) return;
        Parameters parameters = Parameters.server(serverInfo.getServerUUID());
        for (ExtensionMethod provider : graphPointProviders) {
            ProviderIdentifier providerIdentifier = new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), provider.getMethodName());
            try {
                var serverGraphDataSource = new MethodWrapper<>(provider.getMethod(), ServerGraphDataSource.class)
                        .callMethod(extension.getExtension(), parameters);
                if (serverGraphDataSource == null) continue;
                storeGraphMetadata(
                        extension,
                        provider,
                        serverGraphDataSource.getSeriesMetadata(),
                        ExtensionGraphMetadataTable.TableType.SERVER
                );
                ServerGraphSampler sampler = new ServerGraphSampler(
                        serverGraphDataSource,
                        provider.getExistingAnnotation(GraphProvider.class),
                        dbSystem,
                        providerIdentifier,
                        errorLogger
                );
                sampler.register(runnableFactory);
            } catch (DataExtensionMethodCallException e) {
                errorLogger.warn(e, ErrorContext.builder()
                        .related(providerIdentifier)
                        .whatToDo("Graph sampler for " + providerIdentifier.getPluginName() + "." + providerIdentifier.getProviderName() + " ran into error and was not registered.")
                        .build());
            }
        }
    }

    private void storeGraphMetadata(ExtensionWrapper extension, ExtensionMethod provider, List<SeriesMetadata> seriesMetadata, ExtensionGraphMetadataTable.TableType tableType) {
        GraphProvider annotation = provider.getExistingAnnotation(GraphProvider.class);
        ValueBuilder valueBuilder = extension.getExtension().valueBuilder(annotation.displayName())
                .showOnTab(provider.getAnnotationOrNull(Tab.class))
                .methodName(provider)
                .priority(annotation.priority())
                .icon(Icon.called("question").build());
        ProviderInformation info = ((ExtValueBuilder) valueBuilder).buildProviderInfo(annotation);

        dbSystem.getDatabase().executeTransaction(new StoreGraphPointProviderTransaction(annotation, provider, info, serverInfo.getServerUUID(), seriesMetadata, tableType));
    }

    public void registerPlayerGraphSamplers(ExtensionWrapper extension, UUID playerUUID, String playerName) {
        Parameters.PlayerParameters parameters = (Parameters.PlayerParameters) Parameters.player(serverInfo.getServerUUID(), playerUUID, playerName);
        for (var dataSource : playerGraphSources) {
            var provider = dataSource.getLeft();
            var playerDataSource = dataSource.getRight();
            var providerIdentifier = new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), provider.getMethodName());
            PlayerGraphSampler sampler = new PlayerGraphSampler(
                    extension,
                    playerDataSource,
                    provider,
                    dbSystem,
                    parameters,
                    providerIdentifier,
                    errorLogger
            );
            activePlayerGraphSamplers.computeIfAbsent(playerUUID, u -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(sampler);
            sampler.register(runnableFactory);
        }
    }

    public void unregisterPlayerSamplers(UUID playerUUID) {
        Set<PlayerGraphSampler> samplers = activePlayerGraphSamplers.get(playerUUID);
        if (samplers == null) return;

        samplers.forEach(PlayerGraphSampler::unregister);
        samplers.clear();
        activePlayerGraphSamplers.remove(playerUUID);
    }
}
