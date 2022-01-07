/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.PlanVelocity;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.playeranalytics.plugin.server.Listeners;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utilities.TestConstants;
import utilities.mocks.PlanVelocityMocker;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link VelocityPingCounter}.
 *
 * @author AuroraLS3
 */
class VelocityPingCounterTest {

    private PlanVelocity plugin;
    private Player player;

    @BeforeEach
    void setUp() {
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
    void offlinePlayerIsRemovedFromPlayerHistory() {
        VelocityPingCounter counter = new VelocityPingCounter(Mockito.mock(Listeners.class), plugin, null, null, null);

        assertTrue(counter.playerHistory.isEmpty());
        counter.addPlayer(player.getUniqueId());
        assertFalse(counter.playerHistory.isEmpty());

        counter.run();
        assertTrue(counter.playerHistory.isEmpty());
    }

}