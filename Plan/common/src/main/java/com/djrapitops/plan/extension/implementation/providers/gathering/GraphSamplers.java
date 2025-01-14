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

import com.djrapitops.plan.extension.annotation.GraphPointProvider;
import com.djrapitops.plan.extension.annotation.Tab;
import com.djrapitops.plan.extension.builder.ValueBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionMethod;
import com.djrapitops.plan.extension.extractor.ExtensionMethods;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ExtensionWrapper;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.builder.ExtValueBuilder;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.providers.ProviderIdentifier;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreGraphPointProviderTransaction;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

        Parameters parameters = Parameters.server(serverInfo.getServerUUID());
        List<ExtensionMethod> graphPointProviders = extensionMethods.getGraphPointProviders();
        for (ExtensionMethod graphPointProvider : graphPointProviders) {
            storeGraphMetadata(extension, graphPointProvider);

            ServerGraphSampler sampler = new ServerGraphSampler(extension, graphPointProvider, dbSystem, parameters,
                    new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), graphPointProvider.getMethodName()),
                    errorLogger
            );
            sampler.register(runnableFactory);
        }
    }

    private void storeGraphMetadata(ExtensionWrapper extension, ExtensionMethod provider) {
        GraphPointProvider annotation = provider.getExistingAnnotation(GraphPointProvider.class);
        ValueBuilder valueBuilder = extension.getExtension().valueBuilder(annotation.displayName())
                .showOnTab(provider.getAnnotationOrNull(Tab.class))
                .methodName(provider)
                .priority(annotation.priority())
                .icon(Icon.called("question").build());
        ProviderInformation info = ((ExtValueBuilder) valueBuilder).buildProviderInfo(annotation);

        dbSystem.getDatabase().executeTransaction(new StoreGraphPointProviderTransaction(annotation, provider, info, serverInfo.getServerUUID()));
    }

    public void storePlayerGraphMetadata(ExtensionWrapper extension) {
        Map<ExtensionMethod.ParameterType, ExtensionMethods> methods = extension.getMethods();
        for (ExtensionMethod graphPointProvider : methods.get(ExtensionMethod.ParameterType.PLAYER_UUID).getGraphPointProviders()) {
            storeGraphMetadata(extension, graphPointProvider);
        }
        for (ExtensionMethod graphPointProvider : methods.get(ExtensionMethod.ParameterType.PLAYER_STRING).getGraphPointProviders()) {
            storeGraphMetadata(extension, graphPointProvider);
        }
    }

    public void registerPlayerGraphSamplers(ExtensionWrapper extension, UUID playerUUID, String playerName) {
        Parameters parameters = Parameters.player(serverInfo.getServerUUID(), playerUUID, playerName);
        Map<ExtensionMethod.ParameterType, ExtensionMethods> methods = extension.getMethods();
        for (ExtensionMethod graphPointProvider : methods.get(ExtensionMethod.ParameterType.PLAYER_UUID).getGraphPointProviders()) {
            PlayerGraphSampler sampler = new PlayerGraphSampler(extension, graphPointProvider, dbSystem, parameters,
                    new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), graphPointProvider.getMethodName()), errorLogger);
            activePlayerGraphSamplers.computeIfAbsent(playerUUID, u -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(sampler);
            sampler.register(runnableFactory);
        }
        for (ExtensionMethod graphPointProvider : methods.get(ExtensionMethod.ParameterType.PLAYER_STRING).getGraphPointProviders()) {
            PlayerGraphSampler sampler = new PlayerGraphSampler(extension, graphPointProvider, dbSystem, parameters,
                    new ProviderIdentifier(serverInfo.getServerUUID(), extension.getPluginName(), graphPointProvider.getMethodName()), errorLogger);
            activePlayerGraphSamplers.computeIfAbsent(playerUUID, u -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(sampler);
            sampler.register(runnableFactory);
        }
    }

    public void unregisterPlayerSamplers(UUID playerUUID) {
        Set<PlayerGraphSampler> samplers = activePlayerGraphSamplers.getOrDefault(playerUUID, Set.of());
        samplers.forEach(PlayerGraphSampler::unregister);
        samplers.clear();
        activePlayerGraphSamplers.remove(playerUUID);
    }
}
