package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.time.WorldTimes;
import org.junit.Test;
import utilities.TestConstants;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Test for {@link Session} {@link com.djrapitops.plan.data.store.containers.DataContainer}.
 *
 * @author Rsl1122
 */
public class SessionTest {

    private final UUID serverUUID = TestConstants.SERVER_UUID;

    @Test
    public void safeStartKeyConstructor() {
        for (int i = 0; i < 10000; i++) {
            Session session = new Session(null, serverUUID, System.currentTimeMillis(), null, null);

            // Should not throw
            session.getUnsafe(SessionKeys.START);
        }
    }

    @Test
    public void safeStartKeyDBConstructor() {
        for (int i = 0; i < 10000; i++) {
            long time = System.currentTimeMillis();
            Session session = new Session(-1, null, null, time, time + 1, 0, 0, 0);

            // Should not throw
            session.getUnsafe(SessionKeys.START);
        }
    }

    @Test
    public void killsAreAdded() {
        Session session = new Session(null, serverUUID, System.currentTimeMillis(), "", "");

        Optional<List<PlayerKill>> beforeOptional = session.getValue(SessionKeys.PLAYER_KILLS);
        assertTrue(beforeOptional.isPresent());
        List<PlayerKill> before = beforeOptional.get();
        assertTrue(before.isEmpty());

        session.playerKilled(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Weapon", System.currentTimeMillis()));

        Optional<List<PlayerKill>> afterOptional = session.getValue(SessionKeys.PLAYER_KILLS);
        assertTrue(afterOptional.isPresent());
        List<PlayerKill> after = afterOptional.get();

        assertFalse(after.isEmpty());
        assertEquals(before, after);
    }

    @Test
    public void killsAreAdded2() {
        Session session = new Session(null, serverUUID, System.currentTimeMillis(), "", "");

        session.playerKilled(new PlayerKill(TestConstants.PLAYER_TWO_UUID, "Weapon", System.currentTimeMillis()));

        Optional<List<PlayerKill>> afterOptional = session.getValue(SessionKeys.PLAYER_KILLS);
        assertTrue(afterOptional.isPresent());
        List<PlayerKill> after = afterOptional.get();

        assertFalse(after.isEmpty());
    }

    @Test
    public void worldTimesWorks() {
        long time = System.currentTimeMillis();
        Session session = new Session(null, serverUUID, time, "One", "Survival");
        session.changeState("Two", "Three", time + 5L);

        Optional<WorldTimes> optional = session.getValue(SessionKeys.WORLD_TIMES);
        assertTrue(optional.isPresent());
        WorldTimes worldTimes = optional.get();

        assertEquals(5L, worldTimes.getGMTimes("One").getTotal());
    }
}