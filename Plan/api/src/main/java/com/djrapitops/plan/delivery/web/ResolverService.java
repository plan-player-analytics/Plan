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
import com.djrapitops.plan.delivery.web.resolver.request.Request;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Service for modifying webserver request resolution.
 * <p>
 * It is recommended to use plugin based namespace in your custom targets,
 * eg. "/flyplugin/flying" to avoid collisions with other plugins.
 * You can also use {@link #getResolver(String)} to check if target already has a resolver.
 *
 * @author AuroraLS3
 */
public interface ResolverService {

    static ResolverService getInstance() {
        return Optional.ofNullable(ResolverService.Holder.service.get())
                .orElseThrow(() -> new IllegalStateException("ResolverService has not been initialised yet."));
    }

    /**
     * Register a new resolver.
     *
     * @param pluginName Name of the plugin that is registering (For error messages)
     * @param start      Start of the target to match against, eg "/example" will send "/example/target" etc to the Resolver.
     * @param resolver   {@link Resolver} to use for this
     * @throws IllegalArgumentException If pluginName is null or empty.
     */
    void registerResolver(String pluginName, String start, Resolver resolver);

    /**
     * Register a new resolver with regex that matches start of target.
     * <p>
     * NOTICE: It is recommended to avoid too generic regex like "/.*" to not override existing resolvers.
     * <p>
     * Parameters (?param=value) are not included in the regex matching.
     *
     * @param pluginName Name of the plugin that is registering (For error messages)
     * @param pattern    Regex Pattern, "/example.*" will send "/exampletarget" etc to the Resolver.
     * @param resolver   {@link Resolver} to use for this.
     * @throws IllegalArgumentException If pluginName is null or empty.
     */
    void registerResolverForMatches(String pluginName, Pattern pattern, Resolver resolver);

    /**
     * Register a new permission that you are using in your {@link Resolver#canAccess(Request)} method.
     * <p>
     * The permissions are not given to any users by default, and need to be given by admin manually.
     *
     * @param webPermissions Permission strings, higher level permissions grant lower level automatically - eg. page.foo also grants page.foo.bar
     * @return CompletableFuture that tells when the permissions have been stored.
     */
    CompletableFuture<Void> registerPermissions(String... webPermissions);

    /**
     * Register a new permission that you are using in your {@link Resolver#canAccess(Request)} method.
     * <p>
     * The permission is granted to any groups with {@code whenHasPermission} parameter.
     *
     * @param webPermission     Permission string, higher level permissions grant lower level automatically - eg. page.foo also grants page.foo.bar
     * @param whenHasPermission Permission that a group already has that this permission should be granted to - eg. page.network.overview.numbers
     */
    void registerPermission(String webPermission, String whenHasPermission);

    /**
     * Obtain a {@link Resolver} for a target.
     * <p>
     * First matching resolver will be returned.
     * {@link #registerResolver} resolvers have higher priority than {@link #registerResolverForMatches}.
     * <p>
     * Can be used when making Resolver middleware.
     *
     * @param target "/example/target"
     * @return Resolver if registered or empty.
     */
    Optional<Resolver> getResolver(String target);

    /**
     * Obtain all Resolvers that match the target.
     * <p>
     * If first returns Optional.empty next one should be used.
     * <p>
     * Requires Capability PAGE_EXTENSION_RESOLVERS_LIST.
     *
     * @param target "/example/target"
     * @return List of Resolvers if registered or empty list.
     */
    List<Resolver> getResolvers(String target);

    class Holder {
        static final AtomicReference<ResolverService> service = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(ResolverService service) {
            ResolverService.Holder.service.set(service);
        }
    }
}
