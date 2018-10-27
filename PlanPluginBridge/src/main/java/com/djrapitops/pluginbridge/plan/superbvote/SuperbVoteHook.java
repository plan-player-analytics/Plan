package com.djrapitops.pluginbridge.plan.superbvote;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import io.minimum.minecraft.superbvote.SuperbVote;
import io.minimum.minecraft.superbvote.storage.VoteStorage;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to SuperbVote and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
@Singleton
public class SuperbVoteHook extends Hook {

    @Inject
    public SuperbVoteHook() {
        super("io.minimum.minecraft.superbvote.SuperbVote");
    }

    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            VoteStorage store = getPlugin(SuperbVote.class).getVoteStorage();
            handler.addPluginDataSource(new SuperbVoteData(store));
        }
    }
}
