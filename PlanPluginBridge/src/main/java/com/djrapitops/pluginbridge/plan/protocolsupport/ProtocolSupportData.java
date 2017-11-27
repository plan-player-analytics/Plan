/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.protocolsupport;

import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.viaversion.Protocol;
import com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable;
import main.java.com.djrapitops.plan.data.additional.*;

import java.sql.SQLException;
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
public class ProtocolSupportData extends PluginData {

    private final ProtocolTable table;

    public ProtocolSupportData(ProtocolTable table) {
        super(ContainerSize.THIRD, "ProtocolSupport");
        super.setPluginIcon("gamepad");
        super.setIconColor("cyan");
        this.table = table;
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) throws Exception {
        try {
            int protocolVersion = table.getProtocolVersion(uuid);

            inspectContainer.addValue(getWithIcon("Last Join Version", "signal", "light-green"),
                    protocolVersion != -1 ? Protocol.getMCVersion(protocolVersion) : "Not Yet Known");
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
        }

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) throws Exception {
        Map<UUID, Integer> versions;

        try {
            versions = table.getProtocolVersions();
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return analysisContainer;
        }

        Map<UUID, String> userVersions = versions.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> Protocol.getMCVersion(entry.getValue())));

        analysisContainer.addPlayerTableValues(getWithIcon("Last Version", "signal"), userVersions);

        String versionS = getWithIcon("Version", "signal");
        String membersS = getWithIcon("Users", "users");
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