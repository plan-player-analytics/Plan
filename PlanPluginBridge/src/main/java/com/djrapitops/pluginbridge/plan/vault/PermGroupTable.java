/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.vault;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import static org.bukkit.Bukkit.getOfflinePlayers;
import org.bukkit.OfflinePlayer;

/**
 * PluginData class for Vault-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class PermGroupTable extends PluginData {

    private final Permission permSys;

    public PermGroupTable(Permission permSystem) {
        super("Vault", "permgrouptable", AnalysisType.HTML);
        permSys = permSystem;
        String group = Html.FONT_AWESOME_ICON.parse("balance-scale") + " Perm. Group";
        String members = Html.FONT_AWESOME_ICON.parse("users") + " Members";
        super.setPrefix(Html.TABLE_START_2.parse(group, members));
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
        Map<String, Integer> groups = new HashMap<>();
        for (OfflinePlayer p : getOfflinePlayers()) {
            String group = permSys.getPrimaryGroup(null, p);
            if (!groups.containsKey(group)) {
                groups.put(group, 0);
            }
            groups.put(group, groups.get(group) + 1);
        }
        StringBuilder html = new StringBuilder();
        for (String group : groups.keySet()) {
            html.append(Html.TABLELINE_2.parse(StringUtils.capitalize(group), groups.get(group) + ""));
        }
        return html.toString();
    }

}
