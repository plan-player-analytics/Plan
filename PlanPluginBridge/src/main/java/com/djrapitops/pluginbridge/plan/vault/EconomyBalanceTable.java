/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.vault;

import com.djrapitops.pluginbridge.plan.FakeOfflinePlayer;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;
import net.milkbowl.vault.economy.Economy;

import java.io.Serializable;
import java.util.UUID;

/**
 * PluginData class for Vault-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class EconomyBalanceTable extends PluginData {

    private final Economy econ;

    public EconomyBalanceTable(Economy econ) {
        super("Vault", "eco_balance_table", AnalysisType.HTML);
        this.econ = econ;
        String user = Html.FONT_AWESOME_ICON.parse("user") + " Player";
        String balance = Html.FONT_AWESOME_ICON.parse("money") + " Balance";
        super.setPrefix(Html.TABLE_START_2.parse(user, balance));
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
        getUUIDsBeingAnalyzed().forEach(uuid -> {
            String name = getNameOf(uuid);
            String link = Html.LINK.parse(HtmlUtils.getRelativeInspectUrl(name), name);
            String bal = FormatUtils.cutDecimals(econ.getBalance(new FakeOfflinePlayer(uuid, name)));
            html.append(Html.TABLELINE_2.parse(link, bal));
        });
        return html.toString();
    }

}
