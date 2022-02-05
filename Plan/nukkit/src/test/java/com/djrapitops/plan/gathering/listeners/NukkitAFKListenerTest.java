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
package com.djrapitops.plan.gathering.listeners;

import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerMoveEvent;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.listeners.nukkit.NukkitAFKListener;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestConstants;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * Test for {@link NukkitAFKListener}
 *
 * @author AuroraLS3
 */
@ExtendWith(MockitoExtension.class)
class NukkitAFKListenerTest {

    private static NukkitAFKListener underTest;
    private static ErrorLogger errorLogger;

    @BeforeAll
    static void setUp() {
        PlanConfig config = Mockito.mock(PlanConfig.class);
        when(config.get(TimeSettings.AFK_THRESHOLD)).thenReturn(TimeUnit.MINUTES.toMillis(3));
        errorLogger = Mockito.mock(ErrorLogger.class);
        underTest = new NukkitAFKListener(config, errorLogger);

        new SessionCache().cacheSession(TestConstants.PLAYER_ONE_UUID, new ActiveSession(null, null, 0, null, null));
        new SessionCache().cacheSession(TestConstants.PLAYER_TWO_UUID, new ActiveSession(null, null, 0, null, null));
    }

    @AfterEach
    void ensureNoErrors() {
        verifyNoInteractions(errorLogger);
        SessionCache.clear();
    }

    @Test
    void afkPermissionIsNotCalledMoreThanOnceWhenIgnored() {
        Player player = mockPlayerWithPermissions();
        PlayerMoveEvent event = mockMoveEvent(player);

        underTest.onMove(event);
        underTest.onMove(event);

        verify(player, times(1)).hasPermission(anyString());
    }

    @Test
    void afkPermissionIsNotCalledMoreThanOnceWhenNotIgnored() {
        Player player = mockPlayerWithoutPermissions();
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

    private Player mockPlayerWithPermissions() {
        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(TestConstants.PLAYER_ONE_UUID);
        when(player.hasPermission(anyString())).thenReturn(true);
        return player;
    }

    private Player mockPlayerWithoutPermissions() {
        Player player = Mockito.mock(Player.class);
        when(player.getUniqueId()).thenReturn(TestConstants.PLAYER_TWO_UUID);
        when(player.hasPermission(anyString())).thenReturn(false);
        return player;
    }

}