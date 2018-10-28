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
package com.djrapitops.pluginbridge.plan.luckperms;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import me.lucko.luckperms.api.*;
import me.lucko.luckperms.api.caching.MetaData;
import org.apache.commons.text.TextStringBuilder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PluginData for LuckPerms plugin.
 *
 * @author Vankka
 */
class LuckPermsData extends PluginData {

    private final LuckPermsApi api;

    LuckPermsData(LuckPermsApi api) {
        super(ContainerSize.THIRD, "LuckPerms");
        setPluginIcon(Icon.called("exclamation-triangle").of(Color.LIGHT_GREEN).build());

        this.api = api;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        User user = api.getUser(uuid);

        if (user == null) {
            inspectContainer.addValue("Data unavailable", "Could not get user data");
            return inspectContainer;
        }

        MetaData metaData = user.getCachedData().getMetaData(Contexts.allowAll());
        String prefix = metaData.getPrefix();
        String suffix = metaData.getSuffix();

        inspectContainer.addValue(getWithIcon("Primary group", Icon.called("user-friends").of(Family.SOLID)), user.getPrimaryGroup());
        inspectContainer.addValue(getWithIcon("Prefix", Icon.called("file-signature").of(Family.SOLID).of(Color.GREEN)), prefix != null ? prefix : "None");
        inspectContainer.addValue(getWithIcon("Suffix", Icon.called("file-signature").of(Family.SOLID).of(Color.BLUE)),suffix != null ? suffix : "None");

        if (!metaData.getMeta().isEmpty()) {
            TableContainer metaTable = new TableContainer(
                    getWithIcon("Meta", Icon.called("info-circle").of(Family.SOLID)),
                    getWithIcon("Value", Icon.called("file-alt").of(Family.SOLID))
            );
            metaData.getMeta().forEach((key, value) -> metaTable.addRow(key, value));
            inspectContainer.addTable("Meta", metaTable);
        }

        List<String> groups = user.getPermissions().stream()
                                  .filter(Node::isGroupNode)
                                  .map(Node::getGroupName)
                                  .sorted()
                                  .collect(Collectors.toList());

        inspectContainer.addValue(
                getWithIcon("Groups", Icon.called("user-friends").of(Family.SOLID)),
                new TextStringBuilder().appendWithSeparators(groups, ", ").build()
        );

        Set<Track> tracks = api.getTracks();
        if (!tracks.isEmpty()) {
            TableContainer trackTable = new TableContainer(
                    getWithIcon("Track", Icon.called("ellipsis-h").of(Family.SOLID)),
                    getWithIcon("Group", Icon.called("user-friends").of(Family.SOLID))
            );
            for (Track track : tracks) {
                // reduce is used to get the last element
                String currentGroup = api.getGroups().stream()
                                         .map(this::getGroupDisplayName).filter(groups::contains)
                                         .reduce((first, second) -> second).orElse("None");
                trackTable.addRow(track.getName(), currentGroup);
            }
            inspectContainer.addTable("Tracks", trackTable);
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        // There will *always* be atleast 1 group
        TableContainer groupTable = new TableContainer(
                getWithIcon("Group", Icon.called("user-friends").of(Family.SOLID)),
                getWithIcon("Weight", Icon.called("weight-hanging").of(Family.SOLID)),
                getWithIcon("Permissions", Icon.called("list").of(Family.SOLID))
        );

        api.getGroups().stream().sorted(Comparator.comparing(Group::getName)).forEach(group -> {
            OptionalInt weight = group.getWeight();

            groupTable.addRow(getGroupDisplayName(group), weight.isPresent() ? weight.getAsInt() : "None", group.getPermissions().size());
        });
        analysisContainer.addTable("Groups", groupTable);

        Set<Track> tracks = api.getTracks();
        if (!tracks.isEmpty()) {
            TableContainer trackTable = new TableContainer(
                    getWithIcon("Track", Icon.called("ellipsis-h").of(Family.SOLID)),
                    getWithIcon("Size", Icon.called("list").of(Family.SOLID))
            );
            tracks.forEach(track -> trackTable.addRow(track.getName(), track.getSize()));
            analysisContainer.addTable("Tracks", trackTable);
        }

        return analysisContainer;
    }

    private String getGroupDisplayName(Group group) {
        String displayName = group.getDisplayName();
        return displayName != null ? displayName : group.getName();
    }
}
