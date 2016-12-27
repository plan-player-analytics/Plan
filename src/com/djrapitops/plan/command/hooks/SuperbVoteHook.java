package com.djrapitops.plan.command.hooks;

import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
import io.minimum.minecraft.superbvote.SuperbVote;
import java.util.HashMap;
import java.util.UUID;
import static org.bukkit.Bukkit.getOfflinePlayer;
import org.bukkit.OfflinePlayer;

public class SuperbVoteHook implements Hook {

    private Plan plugin;
    private SuperbVote hookP;

    public SuperbVoteHook(Plan plugin) throws Exception {
        this.plugin = plugin;
        this.hookP = SuperbVote.getPlugin();
    }

    @Override
    public HashMap<String, DataPoint> getData(String player) throws Exception {
        HashMap<String, DataPoint> data = new HashMap<>();
        try {
            UUID uuid = UUIDFetcher.getUUIDOf(player);
            OfflinePlayer p = getOfflinePlayer(uuid);
            if (p.hasPlayedBefore()) {
                data.put("SVO-VOTES", new DataPoint("" + hookP.getVoteStorage().getVotes(uuid), DataType.AMOUNT));
            }
        } catch (IllegalArgumentException e) {
        }
        return data;
    }

    @Override
    public HashMap<String, DataPoint> getAllData(String player) throws Exception {
        return getData(player);
    }

}
