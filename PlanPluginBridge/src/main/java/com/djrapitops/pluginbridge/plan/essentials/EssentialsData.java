/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
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
public class EssentialsData extends PluginData {

    private final Essentials essentials;

    public EssentialsData(Essentials essentials) {
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