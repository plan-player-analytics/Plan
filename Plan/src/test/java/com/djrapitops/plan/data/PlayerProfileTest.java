package com.djrapitops.plan.data;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.TimeAmount;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.mocks.SystemMockUtil;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PlayerProfileTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot()).enableConfigSystem();
    }

    @Test
    public void testMaxActivityIndex() {
        PlayerProfile p = new PlayerProfile(null, null, 0L);
        List<Session> sessions = new ArrayList<>();

        long date = MiscUtils.getTime();
        long week = TimeAmount.WEEK.ms();
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        long requiredPlaytime = Settings.ACTIVE_PLAY_THRESHOLD.getNumber() * TimeAmount.MINUTE.ms();
        int requiredLogins = Settings.ACTIVE_LOGIN_THRESHOLD.getNumber();

        for (int i = 0; i < requiredLogins; i++) {
            sessions.add(new Session(0, weekAgo, weekAgo + requiredPlaytime * 4L, 0, 0));
            sessions.add(new Session(0, twoWeeksAgo, twoWeeksAgo + requiredPlaytime * 4L, 0, 0));
            sessions.add(new Session(0, threeWeeksAgo, threeWeeksAgo + requiredPlaytime * 4L, 0, 0));
        }
        p.setSessions(null, sessions);

        assertEquals(5.0, p.getActivityIndex(date).getValue());
    }

    @Test
    public void testMaxActivityIndex2() {
        PlayerProfile p = new PlayerProfile(null, null, 0L);
        List<Session> sessions = new ArrayList<>();

        long date = MiscUtils.getTime();
        long week = TimeAmount.WEEK.ms();
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        long requiredPlaytime = Settings.ACTIVE_PLAY_THRESHOLD.getNumber() * TimeAmount.MINUTE.ms();
        int requiredLogins = Settings.ACTIVE_LOGIN_THRESHOLD.getNumber();

        for (int i = 0; i < requiredLogins * 2; i++) {
            sessions.add(new Session(0, weekAgo, weekAgo + requiredPlaytime * 3L, 0, 0));
            sessions.add(new Session(0, twoWeeksAgo, twoWeeksAgo + requiredPlaytime * 3L, 0, 0));
            sessions.add(new Session(0, threeWeeksAgo, threeWeeksAgo + requiredPlaytime * 3L, 0, 0));
        }
        p.setSessions(null, sessions);

        assertEquals(5.0, p.getActivityIndex(date).getValue());
    }

    @Test
    public void testActivityIndexOne() {
        PlayerProfile p = new PlayerProfile(null, null, 0L);
        List<Session> sessions = new ArrayList<>();

        long date = MiscUtils.getTime();
        long week = TimeAmount.WEEK.ms();
        long weekAgo = date - week;
        long twoWeeksAgo = date - 2L * week;
        long threeWeeksAgo = date - 3L * week;

        int requiredLogins = Settings.ACTIVE_LOGIN_THRESHOLD.getNumber();
        long requiredPlaytime = Settings.ACTIVE_PLAY_THRESHOLD.getNumber() * TimeAmount.MINUTE.ms() / requiredLogins;

        for (int i = 0; i < requiredLogins; i++) {
            sessions.add(new Session(i, weekAgo, weekAgo + requiredPlaytime, 0, 0));
            sessions.add(new Session(i * 2, twoWeeksAgo, twoWeeksAgo + requiredPlaytime, 0, 0));
            sessions.add(new Session(i * 3, threeWeeksAgo, threeWeeksAgo + requiredPlaytime, 0, 0));
        }
        p.setSessions(null, sessions);

        assertTrue(2.0 <= p.getActivityIndex(date).getValue());
    }

    @Test(timeout = 500)
    public void testMethodTimeout() {
        PlayerProfile p = new PlayerProfile(null, null, 0L);
        List<Session> sessions = new ArrayList<>();
        long date = 0;

        for (int i = 0; i < 5000; i++) {
            sessions.add(new Session(0, 0, 0, 0, 0));
        }
        p.setSessions(null, sessions);
        p.getActivityIndex(0);
    }

}