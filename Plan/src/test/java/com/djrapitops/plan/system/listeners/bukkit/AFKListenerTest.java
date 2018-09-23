package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.system.settings.Settings;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import utilities.Teardown;
import utilities.TestConstants;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * Test for {@link AFKListener}
 *
 * @author Rsl1122
 */
public class AFKListenerTest {

    @BeforeClass
    public static void setUpClass() {
        Settings.AFK_THRESHOLD_MINUTES.setTemporaryValue(3);
    }

    @AfterClass
    public static void tearDownClass() {
        Teardown.resetSettingsTempValues();
    }

    @Test
    public void testAfkPermissionCallCaching() {
        AFKListener afkListener = new AFKListener();
        Collection<Boolean> calls = new ArrayList<>();

        Player player = mockPlayer(calls);
        PlayerMoveEvent event = mockMoveEvent(player);

        afkListener.onMove(event);
        assertEquals(1, calls.size());
        afkListener.onMove(event);
        assertEquals(1, calls.size());
    }

    private PlayerMoveEvent mockMoveEvent(Player player) {
        PlayerMoveEvent event = Mockito.mock(PlayerMoveEvent.class);
        doReturn(player).when(event).getPlayer();
        return event;
    }

    private Player mockPlayer(Collection<Boolean> calls) {
        Player player = Mockito.mock(Player.class);
        doReturn(TestConstants.PLAYER_ONE_UUID).when(player).getUniqueId();
        doAnswer(perm -> {
            calls.add(true);
            return true;
        }).when(player).hasPermission(Mockito.anyString());
        return player;
    }

}