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
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.NotReadyException;
import com.djrapitops.plan.extension.annotation.GraphProvider;
import com.djrapitops.plan.extension.annotation.Tab;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.extractor.ExtensionMethods;
import com.djrapitops.plan.extension.graph.*;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.builder.ExtValueBuilder;
import com.djrapitops.plan.extension.implementation.providers.MethodWrapper;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreGraphPointProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StorePlayerGraphPoints;
import com.djrapitops.plan.extension.implementation.storage.transactions.results.StoreServerGraphPoints;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.sql.tables.extension.graph.ExtensionGraphMetadataTable;
import com.djrapitops.plan.utilities.java.ThrowingSupplier;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
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

    private final List<GraphSource<PlayerGraphDataSource>> playerGraphSources = new ArrayList<>();
    private final List<GraphSource<ServerGraphDataSource>> serverGraphSources = new ArrayList<>();
    private final Map<UUID, Set<PlayerGraphSampler>> activePlayerGraphSamplers = new ConcurrentHashMap<>();
    private final Map<String, Set<ServerGraphSampler>> activeServerGraphSamplers = new ConcurrentHashMap<>();

    private final Set<ProviderIdentifier> disabledGraphHistoryIdentifiers = new HashSet<>();

    @Inject
    public GraphSamplers(ServerInfo serverInfo, DBSystem dbSystem, RunnableFactory runnableFactory, ErrorLogger errorLogger) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.runnableFactory = runnableFactory;
        this.errorLogger = errorLogger;
    }

    private static List<SeriesMetadata> wrapException(ThrowingSupplier<List<SeriesMetadata>, RuntimeException> metadataGetter, ProviderIdentifier providerIdentifier) {
        try {
            return metadataGetter.get();
        } catch (NotReadyException | UnsupportedOperationException ignored) {
            // Data or API not available to make the call, no-op.
            return List.of();
        } catch (Exception | IllegalAccessError | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
            throw new DataExtensionMethodCallException("", e, providerIdentifier.getPluginName(), providerIdentifier.getProviderName());
        }
    }

    private static @NonNull String getPrefix(ProviderIdentifier providerIdentifier) {
        return "Graph sampler for " + providerIdentifier.getPluginName() + "." + providerIdentifier.getProviderName();
    }

    public void registerGraphSamplers(ExtensionWrapper extension) {
        Map<ExtensionMethod.ParameterType, ExtensionMethods> methods = extension.getMethods();
        ExtensionMethods extensionMethods = methods.get(ExtensionMethod.ParameterType.SERVER_NONE);

        List<ExtensionMethod> graphPointProviders = extensionMethods.getGraphPointProviders();
        var byReturnType = graphPointProviders.stream()
                .collect(Collectors.groupingBy(ExtensionMethod::getReturnType));
        registerServerProvidersAndSamplers(extension, byReturnType.get(ServerGraphDataSource.class));
        registerPlayerProviders(extension, byReturnType.get(PlayerGraphDataSource.class));
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
                List<SeriesMetadata> seriesMetadata = wrapException(playerGraphDataSource::getSeriesMetadata, providerIdentifier);
                storeGraphMetadata(
                        extension,
                        provider,
                        seriesMetadata,
                        ExtensionGraphMetadataTable.TableType.PLAYER
                );
                playerGraphSources.add(new GraphSource<>(extension, provider, playerGraphDataSource,
                        new AtomicInteger(seriesMetadata != null ? seriesMetadata.size() : 1),
                        () -> storeGraphMetadata(
                                extension,
                                provider,
                                playerGraphDataSource.getSeriesMetadata(),
                                ExtensionGraphMetadataTable.TableType.PLAYER
                        )));
            } catch (DataExtensionMethodCallException e) {
                errorLogger.warn(e, ErrorContext.builder()
                        .related(providerIdentifier)
                        .whatToDo(getPrefix(providerIdentifier) + " ran into error and was not registered.")
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
                List<SeriesMetadata> seriesMetadata = wrapException(serverGraphDataSource::getSeriesMetadata, providerIdentifier);
                storeGraphMetadata(
                        extension,
                        provider,
                        seriesMetadata,
                        ExtensionGraphMetadataTable.TableType.SERVER
                );
                var graphSource = new GraphSource<>(extension, provider, serverGraphDataSource,
                        new AtomicInteger(seriesMetadata != null ? seriesMetadata.size() : 1),
                        () -> storeGraphMetadata(
                                extension,
                                provider,
                                serverGraphDataSource.getSeriesMetadata(),
                                ExtensionGraphMetadataTable.TableType.SERVER
                        ));
                serverGraphSources.add(graphSource);
                ServerGraphSampler sampler = new ServerGraphSampler(
                        serverGraphDataSource,
                        provider.getExistingAnnotation(GraphProvider.class),
                        dbSystem,
                        providerIdentifier,
                        errorLogger,
                        graphSource.refreshMetadata()
                );
                activeServerGraphSamplers.computeIfAbsent(extension.getPluginName(), u -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                        .add(sampler);
                sampler.register(runnableFactory);
            } catch (DataExtensionMethodCallException e) {
                errorLogger.warn(e, ErrorContext.builder()
                        .related(providerIdentifier)
                        .whatToDo(getPrefix(providerIdentifier) + " ran into error and was not registered.")
                        .build());
            }
        }
    }

    private void storeGraphMetadata(ExtensionWrapper extension, ExtensionMethod provider, @Nullable List<SeriesMetadata> seriesMetadata, ExtensionGraphMetadataTable.TableType tableType) {
        GraphProvider annotation = provider.getExistingAnnotation(GraphProvider.class);
        ValueBuilder valueBuilder = extension.getExtension().valueBuilder(annotation.displayName())
                .showOnTab(provider.getAnnotationOrNull(Tab.class))
                .methodName(provider)
                .priority(annotation.priority())
                .icon(Icon.called(annotation.iconName()).of(annotation.iconFamily()).of(annotation.iconColor()).build());
        ProviderInformation info = ((ExtValueBuilder) valueBuilder).buildProviderInfo(annotation);

        dbSystem.getDatabase().executeTransaction(new StoreGraphPointProviderTransaction(annotation, provider, info, serverInfo.getServerUUID(),
                seriesMetadata == null ? List.of() : seriesMetadata,
                tableType));
    }

    public void registerPlayerGraphSamplers(UUID playerUUID, String playerName) {
        Parameters.PlayerParameters parameters = (Parameters.PlayerParameters) Parameters.player(serverInfo.getServerUUID(), playerUUID, playerName);
        for (var graphSource : playerGraphSources) {
            var extension = graphSource.extension();
            var provider = graphSource.method();
            var playerDataSource = graphSource.dataSource();
            var providerIdentifier = new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), provider.getMethodName());
            PlayerGraphSampler sampler = new PlayerGraphSampler(
                    extension,
                    playerDataSource,
                    provider,
                    dbSystem,
                    parameters,
                    providerIdentifier,
                    errorLogger,
                    graphSource.refreshMetadata()
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

    public void updateServerHistory(CallEvents event) {
        for (var graphSource : serverGraphSources) {
            if (shouldGatherEvent(event, graphSource.extension().getCallEvents())) {
                gatherServerHistory(graphSource);
            }
        }
    }

    private void gatherServerHistory(GraphSource<ServerGraphDataSource> graphSource) {
        var extension = graphSource.extension();
        var provider = graphSource.method();
        var providerIdentifier = new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), provider.getMethodName());

        if (disabledGraphHistoryIdentifiers.contains(providerIdentifier)) return;

        HistoryStrategy historyStrategy = provider.getAnnotationOrNull(GraphProvider.class).strategy();
        if (historyStrategy == HistoryStrategy.NO_HISTORY) return;

        try {
            List<DataPoint> pointHistory = graphSource.dataSource().getPointHistory(System.currentTimeMillis());
            if (pointHistory == null || pointHistory.isEmpty()) return;

            int maxColumns = pointHistory.stream()
                    .filter(Objects::nonNull)
                    .filter(point -> !point.getValues().isEmpty())
                    .mapToInt(point -> point.getValues().size()).max()
                    .orElse(1);
            int lastSeenColumns = graphSource.lastSeenColumnCount().get();
            if (maxColumns > lastSeenColumns) {
                graphSource.refreshMetadata().run();
                graphSource.lastSeenColumnCount().set(maxColumns);
            }

            dbSystem.getDatabase().executeTransaction(new StoreServerGraphPoints(historyStrategy, maxColumns, pointHistory, providerIdentifier));
        } catch (NotReadyException | UnsupportedOperationException ignored) {
            // Data or API not available to make the call, no-op.
        } catch (Exception | IllegalAccessError | NoClassDefFoundError | NoSuchFieldError |
                 NoSuchMethodError e) {
            disabledGraphHistoryIdentifiers.add(providerIdentifier);
            errorLogger.warn(e, ErrorContext.builder()
                    .related(providerIdentifier)
                    .whatToDo(getPrefix(providerIdentifier) + " point history method ran into error and was disabled.")
                    .build());
        }
    }

    public void updatePlayerHistory(UUID playerUUID, String playerName, CallEvents event) {
        for (var graphSource : playerGraphSources) {
            if (shouldGatherEvent(event, graphSource.extension().getCallEvents())) {
                gatherPlayerHistory(playerUUID, playerName, graphSource);
            }
        }
    }

    private void gatherPlayerHistory(UUID playerUUID, String playerName, GraphSource<PlayerGraphDataSource> graphSource) {
        var extension = graphSource.extension();
        var provider = graphSource.method();
        var providerIdentifier = new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), provider.getMethodName());

        if (disabledGraphHistoryIdentifiers.contains(providerIdentifier)) return;

        HistoryStrategy historyStrategy = provider.getAnnotationOrNull(GraphProvider.class).strategy();
        if (historyStrategy == HistoryStrategy.NO_HISTORY) return;

        try {
            List<DataPoint> pointHistory = graphSource.dataSource().getPointHistory(System.currentTimeMillis(), playerUUID, playerName);
            if (pointHistory == null || pointHistory.isEmpty()) return;

            int maxColumns = pointHistory.stream()
                    .filter(Objects::nonNull)
                    .filter(point -> !point.getValues().isEmpty())
                    .mapToInt(point -> point.getValues().size()).max()
                    .orElse(1);
            int lastSeenColumns = graphSource.lastSeenColumnCount().get();
            if (maxColumns > lastSeenColumns) {
                graphSource.refreshMetadata().run();
                graphSource.lastSeenColumnCount().set(maxColumns);
            }

            dbSystem.getDatabase().executeTransaction(new StorePlayerGraphPoints(historyStrategy, maxColumns, pointHistory, playerUUID, providerIdentifier));
        } catch (NotReadyException | UnsupportedOperationException ignored) {
            // Data or API not available to make the call, no-op.
        } catch (Exception | IllegalAccessError | NoClassDefFoundError | NoSuchFieldError |
                 NoSuchMethodError e) {
            disabledGraphHistoryIdentifiers.add(providerIdentifier);
            errorLogger.warn(e, ErrorContext.builder()
                    .related(providerIdentifier)
                    .whatToDo(getPrefix(providerIdentifier) + " point history method ran into error and was disabled.")
                    .build());
        }
    }

    private boolean shouldGatherEvent(CallEvents event, CallEvents[] callEvents) {
        if (event == CallEvents.MANUAL) {
            return true;
        }
        return event.isIn(callEvents);
    }

    public void unregister(DataExtension extension) {
        String pluginName = extension.getPluginName();
        Set<ServerGraphSampler> serverGraphSamplers = activeServerGraphSamplers.get(pluginName);
        if (serverGraphSamplers != null) {
            for (ServerGraphSampler serverGraphSampler : serverGraphSamplers) {
                serverGraphSampler.cancel();
            }
        }
        activeServerGraphSamplers.remove(pluginName);
        serverGraphSources.removeIf(source -> pluginName.equals(source.extension().getPluginName()));

        activePlayerGraphSamplers.values().forEach(
                samplers -> {
                    List<PlayerGraphSampler> toRemove = samplers.stream()
                            .filter(sampler -> pluginName.equals(sampler.getPluginName()))
                            .toList();
                    samplers.removeAll(toRemove);
                    toRemove.forEach(PlayerGraphSampler::cancel);
                }
        );
        playerGraphSources.removeIf(source -> pluginName.equals(source.extension().getPluginName()));
    }

    public Map<String, Set<ServerGraphSampler>> getActiveServerGraphSamplers() {
        return activeServerGraphSamplers;
    }
}
