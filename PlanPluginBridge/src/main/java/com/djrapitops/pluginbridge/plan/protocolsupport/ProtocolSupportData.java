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
package com.djrapitops.pluginbridge.plan.protocolsupport;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.element.TableContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.djrapitops.pluginbridge.plan.viaversion.Protocol;
import com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData for ProtocolSupport plugin.
 *
 * @author Rsl1122
 */
class ProtocolSupportData extends PluginData {

    private final Database database;

    ProtocolSupportData(Database database) {
        super(ContainerSize.THIRD, "ProtocolSupport");
        setPluginIcon(Icon.called("gamepad").of(Color.CYAN).build());
        this.database = database;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        try {
            int protocolVersion = database.query(ProtocolTable.getProtocolVersion(uuid));

            inspectContainer.addValue(getWithIcon("Last Join Version", Icon.called("signal").of(Color.CYAN)),
                    protocolVersion != -1 ? Protocol.getMCVersion(protocolVersion) : "Not Yet Known");
        } catch (DBOpException ex) {
            inspectContainer.addValue("Error", ex.toString());
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        Map<UUID, Integer> versions;

        try {
            versions = database.query(ProtocolTable.getProtocolVersions());
        } catch (DBOpException ex) {
            analysisContainer.addValue("Error", ex.toString());
            return analysisContainer;
        }

        Map<UUID, String> userVersions = versions.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Protocol.getMCVersion(entry.getValue())));

        analysisContainer.addPlayerTableValues(getWithIcon("Last Version", Icon.called("signal")), userVersions);

        String versionS = getWithIcon("Version", Icon.called("signal"));
        String membersS = getWithIcon("Users", Icon.called("users"));
        TableContainer versionTable = new TableContainer(versionS, membersS);
        versionTable.setColor("cyan");
        Map<String, Integer> usersPerVersion = getUsersPerVersion(versions);
        for (Map.Entry<String, Integer> entry : usersPerVersion.entrySet()) {
            versionTable.addRow(entry.getKey(), entry.getValue());
        }
        analysisContainer.addTable("versionTable", versionTable);

        return analysisContainer;
    }

    private Map<String, Integer> getUsersPerVersion(Map<UUID, Integer> versions) {
        Map<String, Integer> usersPerVersion = new HashMap<>();

        for (int protocolVersion : versions.values()) {
            String mcVer = Protocol.getMCVersion(protocolVersion);
            if (!usersPerVersion.containsKey(mcVer)) {
                usersPerVersion.put(mcVer, 0);
            }
            usersPerVersion.replace(mcVer, usersPerVersion.get(mcVer) + 1);
        }
        return usersPerVersion;
    }
}