package com.djrapitops.pluginbridge.plan.superbvote;

import com.djrapitops.pluginbridge.plan.Hook;
import io.minimum.minecraft.superbvote.SuperbVote;
import io.minimum.minecraft.superbvote.storage.VoteStorage;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
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
     *
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @see API
     * @throws NoClassDefFoundError when the plugin class can not be found.
     */
    public SuperbVoteHook(HookHandler hookH) throws NoClassDefFoundError {
        super("io.minimum.minecraft.superbvote.SuperbVote");
        if (enabled) {
            VoteStorage store = getPlugin(SuperbVote.class).getVoteStorage();
            hookH.addPluginDataSource(null);
        }
    }
}
