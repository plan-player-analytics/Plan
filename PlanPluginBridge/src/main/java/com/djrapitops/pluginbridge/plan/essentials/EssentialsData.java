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
package com.djrapitops.pluginbridge.plan.essentials;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.UserMap;

import java.util.*;

/**
 * PluginData for Essentials Plugin.
 *
 * @author Rsl1122
 */
class EssentialsData extends PluginData {

    private final Essentials essentials;

    EssentialsData(Essentials essentials) {
        super(ContainerSize.THIRD, "Essentials");
        setPluginIcon(Icon.called("flask").of(Color.DEEP_ORANGE).build());
        this.essentials = essentials;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        if (essentials.getUserMap().userExists(uuid)) {
            User user = essentials.getUser(uuid);

            boolean jailed = user.isJailed();
            boolean muted = user.isMuted();

            inspectContainer.addValue(getWithIcon("Jailed", Icon.called("ban").of(Color.DEEP_ORANGE)), jailed ? "Yes" : "No");
            inspectContainer.addValue(getWithIcon("Muted", Icon.called("bell-slash").of(Color.DEEP_ORANGE)), muted ? "Yes" : "No");
        } else {
            inspectContainer.addValue("No Essentials Data for this user", "-");
        }
        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        UserMap userMap = essentials.getUserMap();

        Map<UUID, String> jailed = new HashMap<>();
        Map<UUID, String> muted = new HashMap<>();
        for (UUID uuid : uuids) {
            if (userMap.userExists(uuid)) {
                User user = essentials.getUser(uuid);
                jailed.put(uuid, user.isJailed() ? "Yes" : "No");
                muted.put(uuid, user.isMuted() ? "Yes" : "No");
            }
        }

        String jailedString = jailed.values().stream().filter("Yes"::equals).count() + " / " + uuids.size();
        String mutedString = muted.values().stream().filter("Yes"::equals).count() + " / " + uuids.size();

        analysisContainer.addValue(getWithIcon("Players in Jail", Icon.called("ban").of(Color.DEEP_ORANGE)), jailedString);
        analysisContainer.addValue(getWithIcon("Muted", Icon.called("bell-slash").of(Color.DEEP_ORANGE)), mutedString);

        analysisContainer.addPlayerTableValues(getWithIcon("Jailed", Icon.called("ban")), jailed);
        analysisContainer.addPlayerTableValues(getWithIcon("Muted", Icon.called("bell-slash")), muted);

        List<String> warpsList = new ArrayList<>(essentials.getWarps().getList());
        if (!warpsList.isEmpty()) {
            TableContainer warps = new TableContainer(
                    getWithIcon("Warp", Icon.called("map-marker-alt")),
                    getWithIcon("Command", Icon.called("terminal"))
            );
            warps.setColor("light-green");

            Collections.sort(warpsList);
            for (String warp : warpsList) {
                warps.addRow(warp, "/warp " + warp);
            }
            analysisContainer.addTable("WarpTable", warps);
        }

        return analysisContainer;
    }
}