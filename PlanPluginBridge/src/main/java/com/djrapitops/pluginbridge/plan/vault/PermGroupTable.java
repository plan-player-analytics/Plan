/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.plugin.utilities.Format;
import com.djrapitops.plugin.utilities.Verify;
import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.html.Html;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * PluginData class for Vault-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class PermGroupTable extends PluginData {

    private final Permission permSys;

    PermGroupTable(Permission permSystem) {
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
        List<FakeOfflinePlayer> userData = Plan.getPlanAPI().getInspectCachedUserData().stream()
                .map(FakeOfflinePlayer::new).collect(Collectors.toList());
        for (OfflinePlayer p : userData) {
            String group = permSys.getPrimaryGroup(null, p);
            if (!groups.containsKey(group)) {
                groups.put(group, 0);
            }
            groups.put(group, groups.get(group) + 1);
        }
        StringBuilder html = new StringBuilder();
        for (String group : groups.keySet()) {
            if (Verify.notNull(group, groups.get(group))) {
                String groupName = Format.create(group).capitalize().toString();
                String groupMembers = groups.get(group) + "";
                html.append(Html.TABLELINE_2.parse(StringUtils.capitalize(group), groups.get(group) + ""));
            }
        }
        return html.toString();
    }

}
