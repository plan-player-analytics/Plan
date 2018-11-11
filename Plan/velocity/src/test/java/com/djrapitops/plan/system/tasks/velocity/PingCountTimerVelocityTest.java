package com.djrapitops.plan.system.tasks.velocity;

import com.djrapitops.plan.PlanVelocity;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import utilities.TestConstants;
import utilities.mocks.PlanVelocityMocker;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link PingCountTimerVelocity}.
 *
 * @author Rsl1122
 */
public class PingCountTimerVelocityTest {

    private PlanVelocity plugin;
    private Player player;

    @Before
    public void setUp() {
        PlanVelocityMocker mocker = PlanVelocityMocker.setUp()
                .withProxy();
        plugin = mocker.getPlanMock();

        player = Mockito.mock(Player.class);
        when(player.getPing()).thenReturn(5L);
        when(player.getUniqueId()).thenReturn(TestConstants.PLAYER_ONE_UUID);

        ProxyServer proxy = plugin.getProxy();
        when(proxy.getPlayer(TestConstants.PLAYER_ONE_UUID)).thenReturn(Optional.empty());
    }

    @Test
    public void offlinePlayerIsRemovedFromPlayerHistory() {
        PingCountTimerVelocity counter = new PingCountTimerVelocity(plugin, null, null, null, null);

        assertTrue(counter.playerHistory.isEmpty());
        counter.addPlayer(player);
        assertFalse(counter.playerHistory.isEmpty());

        counter.run();
        assertTrue(counter.playerHistory.isEmpty());
    }

}