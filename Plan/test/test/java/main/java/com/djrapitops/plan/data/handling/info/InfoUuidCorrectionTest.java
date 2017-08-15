package test.java.main.java.com.djrapitops.plan.data.handling.info;

import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.handling.info.*;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.junit.Test;
import test.java.utils.MockUtils;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

public class InfoUuidCorrectionTest {
    private final UserData wrong = MockUtils.mockUser2();
    private final UUID test = MockUtils.getPlayerUUID();

    @Test
    public void testAllInfoBooleanReturn() {
        long now = MiscUtils.getTime();
        HandlingInfo[] h = new HandlingInfo[]{
                new ChatInfo(test, ""),
                new DeathInfo(test),
                new KickInfo(test),
                new KillInfo(test, now, null, ""),
                new LoginInfo(test, now, null, false, "", "", 0, ""),
                new LogoutInfo(test, now, false, "", null, ""),
                new PlaytimeDependentInfo(test, InfoType.OTHER, now, "", ""),
                new ReloadInfo(test, now, null, false, "", "", "")
        };
        for (HandlingInfo info : h) {
            assertTrue(info.getClass().getSimpleName(), !info.process(wrong));
        }
    }
}
