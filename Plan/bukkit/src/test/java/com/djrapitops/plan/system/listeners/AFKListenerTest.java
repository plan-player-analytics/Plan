package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.system.listeners.bukkit.AFKListener;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import utilities.TestConstants;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * Test for {@link AFKListener}
 *
 * @author Rsl1122
 */
public class AFKListenerTest {

    private AFKListener underTest;

    @Before
    public void setUp() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.get(TimeSettings.AFK_THRESHOLD)).thenReturn(TimeUnit.MINUTES.toMillis(3));
        underTest = new AFKListener(config, new ConsoleErrorLogger(new TestPluginLogger()));
    }

    @Test
    public void afkPermissionIsNotCalledMoreThanOnce() {
        Player player = mockPlayer();
        PlayerMoveEvent event = mockMoveEvent(player);

        underTest.onMove(event);
        underTest.onMove(event);

        verify(player, times(1)).hasPermission(anyString());
    }

    private PlayerMoveEvent mockMoveEvent(Player player) {
        PlayerMoveEvent event = Mockito.mock(PlayerMoveEvent.class);
        when(event.getPlayer()).thenReturn(player);
        return event;
    }

    private Player mockPlayer() {
        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(TestConstants.PLAYER_ONE_UUID);
        when(player.hasPermission(anyString())).thenReturn(true);
        return player;
    }

}