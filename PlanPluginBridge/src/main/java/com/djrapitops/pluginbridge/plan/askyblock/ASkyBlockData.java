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
package com.djrapitops.pluginbridge.plan.askyblock;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.graphs.ProgressBar;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.plugin.utilities.Format;
import com.wasteofplastic.askyblock.ASkyBlockAPI;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * PluginData for ASkyBlock plugin.
 *
 * @author Rsl1122
 */
class ASkyBlockData extends PluginData {

    private final ASkyBlockAPI api;

    private final Formatter<Double> percentageFormatter;

    ASkyBlockData(ASkyBlockAPI api, Formatter<Double> percentageFormatter) {
        super(ContainerSize.THIRD, "ASkyBlock");
        this.percentageFormatter = percentageFormatter;
        setPluginIcon(Icon.called("street-view").of(Color.LIGHT_BLUE).build());
        this.api = api;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        if (api.hasIsland(uuid)) {
            String islandName = api.getIslandName(uuid);
            long level = api.getLongIslandLevel(uuid);
            int resetsLeft = api.getResetsLeft(uuid);

            inspectContainer.addValue(getWithIcon("Island Name", Icon.called("street-view").of(Color.GREEN)), islandName);
            inspectContainer.addValue(getWithIcon("Island Level", Icon.called("street-view").of(Color.AMBER)), level);
            inspectContainer.addValue(getWithIcon("Island Resets Left", Icon.called("refresh").of(Color.GREEN)), resetsLeft);
        } else {
            inspectContainer.addValue(getWithIcon("Island Name", Icon.called("street-view").of(Color.GREEN)), "No Island");
        }

        Map<String, Integer> challengeCompletion = api.getChallengeTimes(uuid);
        int obtained = (int) challengeCompletion.values().stream().filter(value -> value != 0).count();
        int max = challengeCompletion.size();

        inspectContainer.addValue(getWithIcon("Challenge Progress", Icon.called("bookmark").of(Color.LIGHT_BLUE)), obtained + " / " + max);
        ProgressBar challengeProgress = new ProgressBar(obtained, max, "light-blue", percentageFormatter);
        inspectContainer.addHtml("challenge-progress", challengeProgress.toHtml());

        addTable(inspectContainer, challengeCompletion);

        return inspectContainer;
    }

    private void addTable(InspectContainer inspectContainer, Map<String, Integer> challengeCompletion) {
        TableContainer challenges = new TableContainer(
                getWithIcon("Challenge", Icon.called("bookmark")),
                getWithIcon("Times completed", Icon.called("check"))
        );
        challenges.setColor("light-blue");
        challengeCompletion.entrySet().stream()
                .sorted((one, two) -> Integer.compare(two.getValue(), one.getValue()))
                .forEach(entry -> {
                    String challenge = new Format(entry.getKey()).capitalize().toString();
                    Integer completionTimes = entry.getValue();
                    boolean complete = completionTimes > 0;
                    challenges.addRow(
                            "<span" + (complete ? " class=\"col-green\"" : "") + ">" + challenge + "</span>",
                            completionTimes
                    );
                });
        inspectContainer.addTable("challenge-table", challenges);
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        int islandCount = api.getIslandCount();
        String islandWorldName = api.getIslandWorld().getName();

        analysisContainer.addValue(getWithIcon("Island World", Icon.called("map").of(Family.REGULAR).of(Color.GREEN)), islandWorldName);
        analysisContainer.addValue(getWithIcon("Island Count", Icon.called("street-view").of(Color.GREEN)), islandCount);

        return analysisContainer;
    }
}