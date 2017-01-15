package com.djrapitops.planlite.command.hooks;

import com.djrapitops.planlite.api.Hook;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.UUIDFetcher;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.api.DataType;
import io.minimum.minecraft.superbvote.SuperbVote;
import java.util.HashMap;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import static org.bukkit.Bukkit.getOfflinePlayer;

public class SuperbVoteHook implements Hook {

    private PlanLite plugin;
    private SuperbVote hookP;

    public SuperbVoteHook(PlanLite plugin) throws Exception {
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
