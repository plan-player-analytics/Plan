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
package com.djrapitops.plan.delivery.web.resolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility Resolver for organizing resolution in a tree-like structure.
 * <p>
 * CompositeResolver removes first part of the target with {@link URLTarget#omitFirst()}
 * before calling the child Resolvers.
 *
 * @author Rsl1122
 */
public final class CompositeResolver implements Resolver {

    private final List<String> prefixes;
    private final List<Resolver> resolvers;

    CompositeResolver() {
        this.prefixes = new ArrayList<>();
        this.resolvers = new ArrayList<>();
    }

    public static CompositeResolver.Builder builder() {
        return new Builder();
    }

    private Optional<Resolver> getResolver(URLTarget target) {
        return target.getPart(0).flatMap(this::find);
    }

    private Optional<Resolver> find(String prefix) {
        for (int i = 0; i < prefixes.size(); i++) {
            if (prefixes.get(i).equals(prefix)) {
                return Optional.of(resolvers.get(i));
            }
        }
        return Optional.empty();
    }

    void add(String prefix, Resolver resolver) {
        if (prefix == null) throw new IllegalArgumentException("Prefix can not be null");
        if (resolver == null) throw new IllegalArgumentException("Resolver can not be null");
        prefixes.add(prefix);
        resolvers.add(resolver);
    }

    @Override
    public boolean canAccess(WebUser permissions, URLTarget target, Parameters parameters) {
        return getResolver(target)
                .map(resolver -> resolver.canAccess(permissions, target.omitFirst(), parameters))
                .orElse(true);
    }

    @Override
    public Optional<Response> resolve(URLTarget target, Parameters parameters) {
        return getResolver(target)
                .flatMap(resolver -> resolver.resolve(target.omitFirst(), parameters));
    }

    public static class Builder {
        private final CompositeResolver composite;

        private Builder() {
            this.composite = new CompositeResolver();
        }

        /**
         * Add a new resolver to the CompositeResolver.
         *
         * @param prefix   Start of the target (first part of the target string, eg "example" in "/example/target/", or "" in "/")
         * @param resolver Resolver to call for this target, {@link URLTarget#omitFirst()} will be called for Resolver method calls.
         * @return this builder.
         */
        public Builder add(String prefix, Resolver resolver) {
            composite.add(prefix, resolver);
            return this;
        }

        public CompositeResolver build() {
            return composite;
        }
    }
}