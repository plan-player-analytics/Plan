package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.keys.SessionKeys;
import org.junit.Test;

/**
 * Test for {@link Session} {@link com.djrapitops.plan.data.store.containers.DataContainer}.
 *
 * @author Rsl1122
 */
public class SessionTest {

    @Test
    public void safeStartKeyConstructor() {
        for (int i = 0; i < 10000; i++) {
            Session session = new Session(null, System.currentTimeMillis(), null, null);

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

}