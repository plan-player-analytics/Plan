/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;

/**
 * PluginData class for Vault-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class ViaVersionVersionTable extends PluginData {

    private final ProtocolTable table;

    public ViaVersionVersionTable(ProtocolTable table) {
        super("ViaVersion", "versiontable", AnalysisType.HTML);
        this.table = table;
        String version = Html.FONT_AWESOME_ICON.parse("signal") + " Version";
        String members = Html.FONT_AWESOME_ICON.parse("users") + " Users";
        super.setPrefix(Html.TABLE_START_2.parse(version, members));
        super.setSuffix(Html.TABLE_END.parse());
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        String tableLines = getTableLines();
        return parseContainer("", tableLines);
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }

    private String getTableLines() {
        StringBuilder html = new StringBuilder();

        Map<String, Integer> usersPerVersion = getUsersPerVersion(html);
        if (usersPerVersion.isEmpty()) {
            html.append(Html.TABLELINE_2.parse("No joins after 3.5.0 install", ""));
        } else {
            usersPerVersion.entrySet().stream().map(e -> Html.TABLELINE_2.parse(e.getKey(), e.getValue() + "")).forEach(string -> {
                html.append(string);
            });
        }
        return html.toString();
    }

    private Map<String, Integer> getUsersPerVersion(StringBuilder html) {
        Map<String, Integer> usersPerVersion = new HashMap<>();
        try {
            Map<UUID, Integer> versions = table.getProtocolVersions();
            for (int protocolVersion : versions.values()) {
                String mcVer = Protocol.getMCVersion(protocolVersion);
                if (!usersPerVersion.containsKey(mcVer)) {
                    usersPerVersion.put(mcVer, 0);
                }
                usersPerVersion.replace(mcVer, usersPerVersion.get(mcVer) + 1);
            }
        } catch (SQLException ex) {
            html.append(Html.TABLELINE_2.parse(ex.toString(), ""));
            return new HashMap<>();
        }
        return usersPerVersion;
    }

}
