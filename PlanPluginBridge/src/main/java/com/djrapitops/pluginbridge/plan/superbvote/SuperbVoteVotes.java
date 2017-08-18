package com.djrapitops.pluginbridge.plan.superbvote;

import io.minimum.minecraft.superbvote.storage.VoteStorage;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

import java.io.Serializable;
import java.util.UUID;

/**
 * PluginData class for GriefPrevention-plugin.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class SuperbVoteVotes extends PluginData {

    private final VoteStorage store;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param store VoteStorage of SuperbVote
     */
    public SuperbVoteVotes(VoteStorage store) {
        super("SuperbVote", "votes", AnalysisType.INT_TOTAL, AnalysisType.INT_AVG);
        this.store = store;
        super.setAnalysisOnly(false);
        super.setIcon("check");
        super.setPrefix("Votes: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        int votes = store.getVotes(uuid);
        return parseContainer(modifierPrefix, Integer.toString(votes));
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return store.getVotes(uuid);
    }
}
