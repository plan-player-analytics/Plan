package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.listeners.bukkit.*;
import org.bukkit.event.HandlerList;

import javax.inject.Inject;

public class BukkitListenerSystem extends ListenerSystem {

    private final Plan plugin;
    private final PlayerOnlineListener playerOnlineListener;
    private final ChatListener chatListener;
    private final GameModeChangeListener gamemodeChangeListener;
    private final WorldChangeListener worldChangeListener;
    private final CommandListener commandListener;
    private final DeathEventListener deathEventListener;
    private final AFKListener afkListener;

    @Inject
    public BukkitListenerSystem(Plan plugin,
                                PlayerOnlineListener playerOnlineListener,
                                ChatListener chatListener,
                                GameModeChangeListener gamemodeChangeListener,
                                WorldChangeListener worldChangeListener,
                                CommandListener commandListener,
                                DeathEventListener deathEventListener,
                                AFKListener afkListener
    ) {
        this.plugin = plugin;

        this.playerOnlineListener = playerOnlineListener;
        this.chatListener = chatListener;
        this.gamemodeChangeListener = gamemodeChangeListener;
        this.worldChangeListener = worldChangeListener;
        this.commandListener = commandListener;
        this.deathEventListener = deathEventListener;
        this.afkListener = afkListener;
    }

    @Override
    protected void registerListeners() {
        plugin.registerListener(
                playerOnlineListener,
                chatListener,
                gamemodeChangeListener,
                worldChangeListener,
                commandListener,
                deathEventListener,
                afkListener
        );
        PlayerOnlineListener.setCountKicks(true);
    }

    @Override
    protected void unregisterListeners() {
        HandlerList.unregisterAll(plugin);
    }
}
