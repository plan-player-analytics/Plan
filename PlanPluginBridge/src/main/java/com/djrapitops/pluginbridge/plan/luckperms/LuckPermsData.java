/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
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
import com.djrapitops.plan.utilities.html.icon.Icons;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.stream.Collectors;
import me.lucko.luckperms.api.*;
import me.lucko.luckperms.api.caching.MetaData;

public class LuckPermsData extends PluginData {
    private LuckPermsApi api;

    public LuckPermsData(LuckPermsApi api) {
        super(ContainerSize.THIRD, "LuckPerms");
        setPluginIcon(Icon.called("exclamation-triangle").of(Family.SOLID).build());

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

        TableContainer metaTable = new TableContainer(
                getWithIcon("Meta", Icon.called("info-circle").of(Family.SOLID)),
                getWithIcon("Value", Icon.called("file-alt").of(Family.SOLID))
        );
        metaData.getMeta().forEach((key, value) -> metaTable.addRow(key, value));
        inspectContainer.addTable("Meta", metaTable);

        List<String> groups = user.getPermissions().stream().filter(Node::isGroupNode).map(Node::getGroupName).collect(Collectors.toList());

        TableContainer groupTable = new TableContainer(getWithIcon("Group", Icon.called("user-friends").of(Family.SOLID)));
        groups.forEach(groupTable::addRow);
        inspectContainer.addTable("Groups", groupTable);

        TableContainer trackTable = new TableContainer(
                getWithIcon("Track", Icon.called("ellipsis-h").of(Family.SOLID)),
                getWithIcon("Group", Icon.called("user-friends").of(Family.SOLID))
        );
        for (Track track : api.getTracks()) {
            // reduce is used to get the last element
            String currentGroup = api.getGroups().stream().map(this::getGroupDisplayName).filter(groups::contains).reduce((first, second) -> second).orElse("None");
            trackTable.addRow(track.getName(), currentGroup);
        }
        inspectContainer.addTable("Tracks", trackTable);

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> uuids, AnalysisContainer analysisContainer) {
        TableContainer groupTable = new TableContainer(
                getWithIcon("Group", Icon.called("user-friends").of(Family.SOLID)),
                getWithIcon("Weight", Icon.called("weight-hanging").of(Family.SOLID)),
                getWithIcon("Permissions", Icon.called("list").of(Family.SOLID))
        );
        for (Group group : api.getGroups()) {
            OptionalInt weight = group.getWeight();

            groupTable.addRow(getGroupDisplayName(group), weight.isPresent() ? weight.getAsInt() : "None", group.getPermissions().size());
        }
        analysisContainer.addTable("Groups", groupTable);

        TableContainer trackTable = new TableContainer(
                getWithIcon("Track", Icon.called("ellipsis-h").of(Family.SOLID)),
                getWithIcon("Size", Icon.called("list").of(Family.SOLID))
        );
        api.getTracks().forEach(track -> trackTable.addRow(track.getName(), track.getSize()));

        return analysisContainer;
    }

    private String getGroupDisplayName(Group group) {
        String displayName = group.getDisplayName();
        return displayName != null ? displayName : group.getName();
    }
}
