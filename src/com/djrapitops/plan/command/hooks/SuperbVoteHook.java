package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import io.minimum.minecraft.superbvote.SuperbVote;
import java.util.HashMap;

public class SuperbVoteHook implements Hook {

    private Plan plugin;
    private SuperbVote hookP;

    public SuperbVoteHook(Plan plugin) throws Exception {
        this.plugin = plugin;
        this.hookP = SuperbVote.getPlugin();
    }

    @Override
    public HashMap<String, String> getData(String player) throws Exception {
        HashMap<String, String> data = new HashMap<>();
        data.put("SVO-VOTES", "" + hookP.getVoteStorage().getVotes(UUIDFetcher.getUUIDOf(player)));
        return data;
    }

    @Override
    public HashMap<String, String> getAllData(String player) throws Exception {
        return getData(player);
    }

}
