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
import com.djrapitops.plan.version.VersionChecker;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * Placeholder expansion used to provide data from Plan on Bukkit.
 *
 * @author aidn5
 */
public class PlanPlaceholderExtension extends PlaceholderExpansion {

    public final ErrorHandler errorHandler;
    private final VersionChecker versionChecker;
    private final PlanPlaceholders placeholders;

    public PlanPlaceholderExtension(
            PlanPlaceholders placeholders,
            PlanSystem system,
            ErrorHandler errorHandler
    ) {
        this.placeholders = placeholders;
        this.versionChecker = system.getVersionChecker();
        this.errorHandler = errorHandler;
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

    @SuppressWarnings("deprecation")
    @Override
    public String getPlugin() {
        return "Plan";
    }

    @Override
    public String getAuthor() {
        return "Rsl1122";
    }

    @Override
    public String getVersion() {
        return versionChecker.getCurrentVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        try {
            String value = placeholders.onPlaceholderRequest(player.getUniqueId(), params, Collections.emptyList());

            if ("true".equals(value)) { //hack
                value = PlaceholderAPIPlugin.booleanTrue();
            } else if ("false".equals(value)) {
                value = PlaceholderAPIPlugin.booleanFalse();
            }

            return value;
        } catch (Exception e) {
            errorHandler.log(L.WARN, getClass(), e);
            return null;
        }
    }
}
