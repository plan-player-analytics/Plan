package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plan.system.listeners.sponge.*;
import org.spongepowered.api.Sponge;

import javax.inject.Inject;

public class SpongeListenerSystem extends ListenerSystem {

    private final PlanSponge plugin;

    private final SpongeAFKListener afkListener;
    private final SpongeChatListener chatListener;
    private final SpongeCommandListener commandListener;
    private final SpongeDeathListener deathListener;
    private final SpongeGMChangeListener gmChangeListener;
    private final SpongePlayerListener playerListener;
    private final SpongeWorldChangeListener worldChangeListener;

    @Inject
    public SpongeListenerSystem(PlanSponge plugin,
                                SpongeAFKListener afkListener,
                                SpongeChatListener chatListener,
                                SpongeCommandListener commandListener,
                                SpongeDeathListener deathListener,
                                SpongeGMChangeListener gmChangeListener,
                                SpongePlayerListener playerListener,
                                SpongeWorldChangeListener worldChangeListener
    ) {
        this.plugin = plugin;

        this.afkListener = afkListener;
        this.chatListener = chatListener;
        this.commandListener = commandListener;
        this.deathListener = deathListener;
        this.gmChangeListener = gmChangeListener;
        this.playerListener = playerListener;
        this.worldChangeListener = worldChangeListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(
                afkListener,
                chatListener,
                commandListener,
                deathListener,
                playerListener,
                gmChangeListener,
                worldChangeListener
        );
    }

    @Override
    protected void unregisterListeners() {
        Sponge.getEventManager().unregisterPluginListeners(plugin);
    }
}
