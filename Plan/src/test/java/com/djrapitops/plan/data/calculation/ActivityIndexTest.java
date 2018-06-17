package com.djrapitops.plan.data.calculation;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.TestConstants;
import utilities.mocks.SystemMockUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test for ActivityIndex.
 *
 * @author Rsl1122
 */
public class ActivityIndexTest {
    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();
    private static final UUID UUID = TestConstants.PLAYER_ONE_UUID;
    private static final UUID SERVER_UUID = TestConstants.SERVER_UUID;

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot()).enableConfigSystem();
    }

    @Test
    public void testMaxActivityIndex() {
        PlayerContainer container = new PlayerContainer();
        List<Session> sessions = new ArrayList<>();

        long date = System.currentTimeMillis();
        long week = TimeAmount.WEEK.ms();
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        long requiredPlaytime = Settings.ACTIVE_PLAY_THRESHOLD.getNumber() * TimeAmount.MINUTE.ms();
        int requiredLogins = Settings.ACTIVE_LOGIN_THRESHOLD.getNumber();

        for (int i = 0; i < requiredLogins; i++) {
            sessions.add(new Session(0, UUID, SERVER_UUID, weekAgo, weekAgo + requiredPlaytime * 4L, 0, 0, 0));
            sessions.add(new Session(0, UUID, SERVER_UUID, twoWeeksAgo, twoWeeksAgo + requiredPlaytime * 4L, 0, 0, 0));
            sessions.add(new Session(0, UUID, SERVER_UUID, threeWeeksAgo, threeWeeksAgo + requiredPlaytime * 4L, 0, 0, 0));
        }
        container.putRawData(PlayerKeys.SESSIONS, sessions);

        assertEquals(5.0, new ActivityIndex(container, date).getValue());
    }

    @Test
    public void testMaxActivityIndex2() {
        PlayerContainer container = new PlayerContainer();
        List<Session> sessions = new ArrayList<>();

        long date = System.currentTimeMillis();
        long week = TimeAmount.WEEK.ms();
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        long requiredPlaytime = Settings.ACTIVE_PLAY_THRESHOLD.getNumber() * TimeAmount.MINUTE.ms();
        int requiredLogins = Settings.ACTIVE_LOGIN_THRESHOLD.getNumber();

        for (int i = 0; i < requiredLogins * 2; i++) {
            sessions.add(new Session(0, UUID, SERVER_UUID, weekAgo, weekAgo + requiredPlaytime * 3L, 0, 0, 0));
            sessions.add(new Session(0, UUID, SERVER_UUID, twoWeeksAgo, twoWeeksAgo + requiredPlaytime * 3L, 0, 0, 0));
            sessions.add(new Session(0, UUID, SERVER_UUID, threeWeeksAgo, threeWeeksAgo + requiredPlaytime * 3L, 0, 0, 0));
        }
        container.putRawData(PlayerKeys.SESSIONS, sessions);
        assertTrue(container.supports(PlayerKeys.SESSIONS));
        assertTrue(container.getValue(PlayerKeys.SESSIONS).isPresent());

        assertEquals(5.0, new ActivityIndex(container, date).getValue());
    }

    @Test
    public void testActivityIndexOne() {
        PlayerContainer container = new PlayerContainer();
        List<Session> sessions = new ArrayList<>();

        long date = System.currentTimeMillis();
        long week = TimeAmount.WEEK.ms();
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        int requiredLogins = Settings.ACTIVE_LOGIN_THRESHOLD.getNumber();
        long requiredPlaytime = Settings.ACTIVE_PLAY_THRESHOLD.getNumber() * TimeAmount.MINUTE.ms() / requiredLogins;

        for (int i = 0; i < requiredLogins; i++) {
            sessions.add(new Session(i, UUID, SERVER_UUID, weekAgo, weekAgo + requiredPlaytime, 0, 0, 0));
            sessions.add(new Session(i * 2, UUID, SERVER_UUID, twoWeeksAgo, twoWeeksAgo + requiredPlaytime, 0, 0, 0));
            sessions.add(new Session(i * 3, UUID, SERVER_UUID, threeWeeksAgo, threeWeeksAgo + requiredPlaytime, 0, 0, 0));
        }
        container.putRawData(PlayerKeys.SESSIONS, sessions);

        assertTrue(2.0 <= new ActivityIndex(container, date).getValue());
    }

    @Test(timeout = 500)
    public void testTimeout() {
        PlayerContainer container = new PlayerContainer();
        List<Session> sessions = new ArrayList<>();
        long date = 0;

        for (int i = 0; i < 5000; i++) {
            sessions.add(new Session(0, UUID, SERVER_UUID, 0, 0, 0, 0, 0));
        }
        container.putRawData(PlayerKeys.SESSIONS, sessions);

        new ActivityIndex(container, 0).getValue();
    }
}