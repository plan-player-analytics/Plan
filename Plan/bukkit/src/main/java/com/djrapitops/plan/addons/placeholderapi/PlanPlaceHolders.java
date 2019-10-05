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
import com.djrapitops.plan.addons.placeholderapi.placeholders.*;
import com.djrapitops.plan.delivery.domain.keys.ServerKeys;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.version.VersionCheckSystem;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Placeholder expansion used to provide data from Plan.
 *
 * <p>
 * <b>Current used services for placeholders:</b>
 * <ul>
 * <li>{@link ServerPlaceHolders}:
 * {@link ServerKeys#TPS},{@link ServerKeys#NAME},
 * {@link ServerKeys#SERVER_UUID}</li>
 * <li>{@link OperatorPlaceholders}: {@link ServerKeys#OPERATORS}</li>
 * <li>{@link WorldTimePlaceHolder}: {@link ServerKeys#WORLD_TIMES}</li>
 * <li>{@link SessionPlaceHolder}: {@link ServerKeys#SESSIONS},
 * {@link ServerKeys#PLAYERS},{@link ServerKeys#PING},{@link ServerKeys#ALL_TIME_PEAK_PLAYERS},
 * {@link ServerKeys#RECENT_PEAK_PLAYERS}</li>
 * </ul>
 *
 * @author aidn5
 */
public class PlanPlaceHolders extends PlaceholderExpansion {
    public final ErrorHandler errorHandler;

    private final Collection<AbstractPlanPlaceHolder> placeholders = new ArrayList<>();
    private final VersionCheckSystem versionCheckSystem;

    public PlanPlaceHolders(
            PlanSystem system,
            ErrorHandler errorHandler
    ) {
        this.versionCheckSystem = system.getVersionCheckSystem();
        this.errorHandler = errorHandler;

        PlanConfig config = system.getConfigSystem().getConfig();
        DBSystem databaseSystem = system.getDatabaseSystem();
        ServerInfo serverInfo = system.getServerInfo();
        Formatters formatters = system.getDeliveryUtilities().getFormatters();

        placeholders.add(new ServerPlaceHolders(databaseSystem, serverInfo, formatters));
        placeholders.add(new OperatorPlaceholders(databaseSystem, serverInfo));
        placeholders.add(new WorldTimePlaceHolder(databaseSystem, serverInfo, formatters));
        placeholders.add(new SessionPlaceHolder(config, databaseSystem, serverInfo, formatters));
        placeholders.add(new PlayerPlaceHolder(databaseSystem, serverInfo, formatters));
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
    public String getPlugin() {
        return "Plan";
    }

    @Override
    public String getAuthor() {
        return "Rsl1122";
    }

    @Override
    public String getVersion() {
        return versionCheckSystem.getCurrentVersion();
    }

    @Override
    public String onPlaceholderRequest(Player p, String params) {
        try {
            for (AbstractPlanPlaceHolder placeholder : placeholders) {
                String value = placeholder.onPlaceholderRequest(p, params);
                if (value == null) continue;

                return value;

            }
        } catch (Exception e) {
            errorHandler.log(L.WARN, getClass(), e);
            e.printStackTrace();
        }

        return null;
    }
}
