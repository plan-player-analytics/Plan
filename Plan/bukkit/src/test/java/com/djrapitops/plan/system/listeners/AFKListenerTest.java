package com.djrapitops.plan.system.listeners;

import com.djrapitops.plan.system.listeners.bukkit.AFKListener;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import utilities.TestConstants;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
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
        when(config.getNumber(Settings.AFK_THRESHOLD_MINUTES)).thenReturn(3);
        underTest = new AFKListener(config, new ConsoleErrorLogger(new TestPluginLogger()));
    }

    @Test
    public void afkPermissionIsNotCalledMoreThanOnce() {
        Collection<Boolean> calls = new ArrayList<>();

        Player player = mockPlayer(calls);
        PlayerMoveEvent event = mockMoveEvent(player);

        underTest.onMove(event);
        assertEquals(1, calls.size());
        underTest.onMove(event);
        assertEquals(1, calls.size());
    }

    private PlayerMoveEvent mockMoveEvent(Player player) {
        PlayerMoveEvent event = Mockito.mock(PlayerMoveEvent.class);
        doReturn(player).when(event).getPlayer();
        return event;
    }

    private Player mockPlayer(Collection<Boolean> calls) {
        Player player = Mockito.mock(Player.class);
        Mockito.doReturn(TestConstants.PLAYER_ONE_UUID).when(player).getUniqueId();
        doAnswer(perm -> {
            calls.add(true);
            return true;
        }).when(player).hasPermission(Mockito.anyString());
        return player;
    }

}