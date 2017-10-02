/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.superbvote;

import io.minimum.minecraft.superbvote.storage.VoteStorage;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.html.Html;

import java.io.Serializable;
import java.util.UUID;

/**
 * PluginData class for Vault-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class SuperbVoteVotesTable extends PluginData {

    private final VoteStorage store;

    public SuperbVoteVotesTable(VoteStorage store) {
        super("SuperbVote", "vote_table", AnalysisType.HTML);
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
        getUUIDsBeingAnalyzed()
                .forEach(uuid -> {
                    String name = getNameOf(uuid);
                    String link = Html.LINK.parse(Plan.getPlanAPI().getPlayerInspectPageLink(name), name);
                    String bal = FormatUtils.cutDecimals(store.getVotes(uuid));
                    html.append(Html.TABLELINE_2.parse(link, bal));
                });
        return html.toString();
    }

}
