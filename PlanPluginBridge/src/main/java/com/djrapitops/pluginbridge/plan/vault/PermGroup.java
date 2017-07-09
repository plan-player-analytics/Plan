/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.vault;

import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

/**
 * PluginData class for Vault-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class PermGroup extends PluginData {

    private final Permission permSys;

    public PermGroup(Permission permSystem) {
        super("Vault", "permgroup");
        permSys = permSystem;
        super.setIcon("balance-scale");
        super.setPrefix("Permission Group: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (!p.hasPlayedBefore()) {
            return parseContainer("", "Hasn't played.");
        }
        String group = permSys.getPrimaryGroup(null, p);
        return parseContainer(modifierPrefix, StringUtils.capitalize(group));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return -1;
    }
}
