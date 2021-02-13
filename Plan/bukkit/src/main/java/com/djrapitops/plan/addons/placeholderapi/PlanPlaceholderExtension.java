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
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import com.djrapitops.plugin.logging.L;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.UUID;

/**
 * Placeholder expansion used to provide data from Plan on Bukkit.
 *
 * @author aidn5
 */
public class PlanPlaceholderExtension extends PlaceholderExpansion {

    private final ErrorLogger errorLogger;
    private final VersionChecker versionChecker;
    private final PlanPlaceholders placeholders;

    public PlanPlaceholderExtension(
            PlanPlaceholders placeholders,
            PlanSystem system,
            ErrorLogger errorLogger
    ) {
        this.placeholders = placeholders;
        this.versionChecker = system.getVersionChecker();
        this.errorLogger = errorLogger;
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

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        UUID uuid = player != null ? player.getUniqueId() : null;
        if ("Server thread".equalsIgnoreCase(Thread.currentThread().getName())) {
            return "[placeholder replacement on server thread is not supported by Plan because it can crash the server!]";
        }
        try {
            String value = placeholders.onPlaceholderRequest(uuid, params, Collections.emptyList());

            if ("true".equals(value)) { //hack
                value = PlaceholderAPIPlugin.booleanTrue();
            } else if ("false".equals(value)) {
                value = PlaceholderAPIPlugin.booleanFalse();
            }

            return value;
        } catch (Exception e) {
            errorLogger.log(L.WARN, e, ErrorContext.builder().whatToDo("Report this").related("Placeholder Request", params, uuid).build());
            return null;
        }
    }
}
