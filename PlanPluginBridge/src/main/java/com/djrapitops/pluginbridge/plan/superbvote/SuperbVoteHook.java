package com.djrapitops.pluginbridge.plan.superbvote;

import com.djrapitops.pluginbridge.plan.Hook;
import io.minimum.minecraft.superbvote.SuperbVote;
import io.minimum.minecraft.superbvote.storage.VoteStorage;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.plugin.HookHandler;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to SuperbVote and registering data
 * sources.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class SuperbVoteHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     * @see API
     */
    public SuperbVoteHook(HookHandler hookH) {
        super("io.minimum.minecraft.superbvote.SuperbVote", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            VoteStorage store = getPlugin(SuperbVote.class).getVoteStorage();
            addPluginDataSource(new SuperbVoteData(store));
        }
    }
}
