package main.java.com.djrapitops.plan.data.additional;

import io.minimum.minecraft.superbvote.SuperbVote;
import io.minimum.minecraft.superbvote.storage.VoteStorage;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;

/**
 *
 * @author Rsl1122
 */
public class SuperbVoteHook extends Hook {

    private final Plan plugin;
    private VoteStorage votes;

    /**
     * Hooks to SuperbVote plugin
     *
     * @param plugin
     */
    public SuperbVoteHook(Plan plugin) {
        super("io.minimum.minecraft.superbvote.SuperbVote");
        this.plugin = plugin;
    }

    /**
     * Grabs votes from SuperbVote.
     * isEnabled() should be called before this
     * method.
     *
     * @param uuid UUID of player
     * @return Amount of votes
     */
    public int getVotes(UUID uuid) {
        return votes.getVotes(uuid);
    }
}
