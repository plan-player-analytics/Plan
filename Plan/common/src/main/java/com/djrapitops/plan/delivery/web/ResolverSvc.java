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

import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.webuser.GrantWebPermissionToGroupsWithPermissionTransaction;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreMissingWebPermissionsTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * ResolverService Implementation.
 *
 * @author AuroraLS3
 */
@Singleton
public class ResolverSvc implements ResolverService {

    private final PlanConfig config;
    private final PluginLogger logger;
    private final DBSystem dbSystem;

    private final List<Container> basicResolvers;
    private final List<Container> regexResolvers;

    @Inject
    public ResolverSvc(PlanConfig config, PluginLogger logger, DBSystem dbSystem) {
        this.config = config;
        this.logger = logger;
        this.dbSystem = dbSystem;
        basicResolvers = new ArrayList<>();
        regexResolvers = new ArrayList<>();
    }

    public void register() {
        ResolverService.Holder.set(this);
    }

    @Override
    public void registerResolver(String pluginName, String start, Resolver resolver) {
        basicResolvers.add(new Container(pluginName, checking -> checking.startsWith(start), resolver, start));
        Collections.sort(basicResolvers);
        Set<String> usedWebPermissions = resolver.usedWebPermissions();
        dbSystem.getDatabase().executeTransaction(new StoreMissingWebPermissionsTransaction(usedWebPermissions));
        if (config.isTrue(PluginSettings.DEV_MODE)) {
            logger.info("Registered basic resolver '" + start + "' for plugin " + pluginName);
        }
    }

    @Override
    public void registerResolverForMatches(String pluginName, Pattern pattern, Resolver resolver) {
        regexResolvers.add(new Container(pluginName, pattern.asPredicate(), resolver, pattern.pattern()));
        Collections.sort(regexResolvers);
        if (config.isTrue(PluginSettings.DEV_MODE)) {
            logger.info("Registered regex resolver '" + pattern.pattern() + "' for plugin " + pluginName);
        }
    }

    @Override
    public CompletableFuture<Void> registerPermissions(String... webPermissions) {
        return dbSystem.getDatabase().executeTransaction(new StoreMissingWebPermissionsTransaction(Arrays.asList(webPermissions)))
                .thenRun(() -> {});
    }

    @Override
    public void registerPermission(String webPermission, String whenHasPermission) {
        registerPermissions(webPermission)
                .thenRun(() -> dbSystem.getDatabase().executeTransaction(
                        new GrantWebPermissionToGroupsWithPermissionTransaction(webPermission, whenHasPermission)
                ));
    }

    @Override
    public Optional<Resolver> getResolver(String target) {
        for (Container container : basicResolvers) {
            if (container.matcher.test(target)) return Optional.of(container.resolver);
        }
        for (Container container : regexResolvers) {
            if (container.matcher.test(target)) return Optional.of(container.resolver);
        }
        return Optional.empty();
    }

    @Override
    public List<Resolver> getResolvers(@Untrusted String target) {
        boolean devMode = config.isTrue(PluginSettings.DEV_MODE);
        List<Resolver> resolvers = new ArrayList<>();
        for (Container container : basicResolvers) {
            if (container.matcher.test(target)) {
                if (devMode) logger.info("Match " + target + " - " + container.plugin + " '" + container.sortBy + "'");
                resolvers.add(container.resolver);
            }
        }
        for (Container container : regexResolvers) {
            if (container.matcher.test(target)) {
                if (devMode) logger.info("Match " + target + " - " + container.plugin + " '" + container.sortBy + "'");
                resolvers.add(container.resolver);
            }
        }
        return resolvers;
    }

    public Optional<String> getPluginInChargeOf(String target) {
        for (Container container : basicResolvers) {
            if (container.matcher.test(target)) return Optional.of(container.plugin);
        }
        for (Container container : regexResolvers) {
            if (container.matcher.test(target)) return Optional.of(container.plugin);
        }
        return Optional.empty();
    }

    private static class Container implements Comparable<Container> {
        final String plugin;
        final Predicate<String> matcher;
        final Resolver resolver;
        final String sortBy;

        public Container(String plugin, Predicate<String> matcher, Resolver resolver, String sortBy) {
            this.plugin = plugin;
            this.matcher = matcher;
            this.resolver = resolver;
            this.sortBy = sortBy;
        }

        @Override
        public int compareTo(Container o) {
            // Longest first
            return Integer.compare(o.sortBy.length(), this.sortBy.length());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Container)) return false;
            Container container = (Container) o;
            return Objects.equals(plugin, container.plugin) &&
                    Objects.equals(matcher, container.matcher) &&
                    Objects.equals(resolver, container.resolver) &&
                    Objects.equals(sortBy, container.sortBy);
        }

        @Override
        public int hashCode() {
            return Objects.hash(plugin, matcher, resolver, sortBy);
        }
    }
}