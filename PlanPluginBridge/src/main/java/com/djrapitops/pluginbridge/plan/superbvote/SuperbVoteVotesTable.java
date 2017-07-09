/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.superbvote;

import io.minimum.minecraft.superbvote.storage.VoteStorage;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;

/**
 * PluginData class for Vault-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class SuperbVoteVotesTable extends PluginData {

    private final VoteStorage store;

    public SuperbVoteVotesTable(VoteStorage store) {
        super("SuperbVote", "votetable", AnalysisType.HTML);
        this.store = store;
        String user = Html.FONT_AWESOME_ICON.parse("user") + " Player";
        String votes = Html.FONT_AWESOME_ICON.parse("check") + " Votes";
        super.setPrefix(Html.TABLE_START_2.parse(user, votes));
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
        Plan.getPlanAPI().getInspectCachedUserData().stream()
                .forEach(data -> {
                    String link = Html.LINK.parse(HtmlUtils.getInspectUrl(data.getName()), data.getName());
                    String bal = FormatUtils.cutDecimals(store.getVotes(data.getUuid()));
                    html.append(Html.TABLELINE_2.parse(link, bal));
                });
        return html.toString();
    }

}
