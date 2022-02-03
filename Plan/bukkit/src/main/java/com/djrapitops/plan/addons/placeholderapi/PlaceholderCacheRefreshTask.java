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

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import me.clip.placeholderapi.PlaceholderAPI;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class PlaceholderCacheRefreshTask extends TaskSystem.Task {

    private final PlanConfig config;

    @Inject
    public PlaceholderCacheRefreshTask(PlanConfig config) {
        this.config = config;
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this).runTaskTimer(
                TimeAmount.toTicks(60, TimeUnit.SECONDS),
                TimeAmount.toTicks(15, TimeUnit.SECONDS)
        );
    }

    @Override
    public void run() {
        if (!config.getNode("Plugins.PlaceholderAPI").isPresent()) {
            cancel(); // Cancel the task and don't do anything if PlaceholderAPI is not installed.
            return;
        }
        List<String> placeholders = config.getStringList("Plugins.PlaceholderAPI.Load_these_placeholders_on_join");
        if (placeholders.isEmpty() || placeholders.size() == 1 && placeholders.contains("%plan_server_uuid%")) {
            // Don't do anything if using default settings or placeholder api is not installed
            return;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            PlaceholderAPI.setPlaceholders(onlinePlayer, placeholders.toString());
        }
    }
}
