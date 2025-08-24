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
package com.djrapitops.plan.addons.placeholderapi;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.placeholder.PlanPlaceholders;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Placeholder expansion used to provide data from Plan on Bukkit.
 *
 * @author aidn5
 */
public class PlanPlaceholderExtension extends PlaceholderExpansion {

    private final ErrorLogger errorLogger;
    private final VersionChecker versionChecker;
    private final PlanPlaceholders placeholders;

    private final Set<String> currentlyProcessing;
    private final Processing processing;
    private final Cache<String, String> cache;

    public PlanPlaceholderExtension(
            PlanPlaceholders placeholders,
            PlanSystem system,
            ErrorLogger errorLogger
    ) {
        this.placeholders = placeholders;
        processing = system.getProcessing();
        this.versionChecker = system.getVersionChecker();
        this.errorLogger = errorLogger;

        currentlyProcessing = Collections.newSetFromMap(new ConcurrentHashMap<>());
        cache = Caffeine.newBuilder()
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "plan";
    }

    @Override
    public String getAuthor() {
        return "AuroraLS3";
    }

    @Override
    public String getVersion() {
        return versionChecker.getCurrentVersion();
    }

    private static boolean isServerThread() {
        String threadName = Thread.currentThread().getName();
        return "Server thread".equalsIgnoreCase(threadName) // Spigot
                || threadName != null && threadName.contains("Region Scheduler Thread"); // Folia
    }

    @Override
    public String onRequest(OfflinePlayer player, @Untrusted String params) {
        try {
            UUID uuid = player != null ? player.getUniqueId() : null;
            if (isServerThread()) {
                return getCached(params, uuid);
            }

            return Optional.ofNullable(getCached(params, uuid))
                    .orElseGet(() -> getPlaceholderValue(params, uuid));
        } catch (IllegalStateException e) {
            if ("zip file closed".equals(e.getMessage())) {
                return null; // Plan is disabled.
            } else {
                throw e;
            }
        }
    }

    private String getPlaceholderValue(@Untrusted String params, UUID uuid) {
        try {
            String value = placeholders.onPlaceholderRequest(uuid, parseRequest(params), parseParameters(params));

            if ("true".equals(value)) { //hack
                value = PlaceholderAPIPlugin.booleanTrue();
            } else if ("false".equals(value)) {
                value = PlaceholderAPIPlugin.booleanFalse();
            }

            return value;
        } catch (Exception e) {
            errorLogger.warn(e, ErrorContext.builder().whatToDo("Report this").related("Placeholder Request", params, uuid).build());
            return null;
        }
    }

    @Untrusted
    private String parseRequest(@Untrusted String params) {
        return params.split(":")[0];
    }

    @Untrusted
    private List<String> parseParameters(@Untrusted String params) {
        List<String> parameters = new ArrayList<>();
        boolean first = true;
        for (@Untrusted String parameter : params.split(":")) {
            if (first) {
                first = false;
            } else {
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    private String getCached(@Untrusted String params, UUID uuid) {
        @Untrusted String key = params + "-" + uuid;

        if (!currentlyProcessing.contains(key)) {
            currentlyProcessing.add(key);
            processing.submitNonCritical(() -> {
                String value = getPlaceholderValue(params, uuid);

                if (value != null) {
                    cache.put(key, value);
                }
                currentlyProcessing.remove(key);
            });
        }

        return cache.getIfPresent(key);
    }
}
