package test.java.main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.locale.Message;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import main.java.com.djrapitops.plan.utilities.comparators.*;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ComparatorTest {

    private Random r = new Random();

    @Test
    public void testHandlingInfoComparator() {
        List<HandlingInfo> test = RandomData.randomHandlingInfo();
        List<Long> longValues = test.stream().map(HandlingInfo::getTime).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new HandlingInfoTimeComparator());
        List<Long> afterSort = test.stream().map(HandlingInfo::getTime).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testPointComparator() {
        List<Point> test = RandomData.randomPoints();

        List<Long> longValues = test.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new PointComparator());
        List<Long> afterSort = test.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testSessionDataComparator() {
        List<SessionData> test = RandomData.randomSessions();
        List<Long> longValues = test.stream().map(SessionData::getSessionStart).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new SessionDataComparator());
        List<Long> afterSort = test.stream().map(SessionData::getSessionStart).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testTPSComparator() {
        List<TPS> test = RandomData.randomTPS();
        List<Long> longValues = test.stream().map(TPS::getDate).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new TPSComparator());
        List<Long> afterSort = test.stream().map(TPS::getDate).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testUserDataLastPlayedComparator() {
        List<UserData> test = RandomData.randomUserData();
        List<Long> longValues = test.stream().map(UserData::getLastPlayed).collect(Collectors.toList());
        longValues.sort(Long::compare);
        Collections.reverse(longValues);
        test.sort(new UserDataLastPlayedComparator());
        List<Long> afterSort = test.stream().map(UserData::getLastPlayed).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }


    @Test
    public void testUserDataNameComparator() {
        List<UserData> test = RandomData.randomUserData();
        List<String> stringValues = test.stream().map(UserData::getName).collect(Collectors.toList());
        Collections.sort(stringValues);
        test.sort(new UserDataNameComparator());
        List<String> afterSort = test.stream().map(UserData::getName).collect(Collectors.toList());
        assertEquals(stringValues, afterSort);
    }

    @Test
    public void testWebUserComparator() throws PassEncryptUtil.CannotPerformOperationException {
        List<WebUser> test = RandomData.randomWebUsers();
        List<Integer> intValues = test.stream().map(WebUser::getPermLevel).collect(Collectors.toList());
        intValues.sort(Integer::compare);
        Collections.reverse(intValues);
        test.sort(new WebUserComparator());
        List<Integer> afterSort = test.stream().map(WebUser::getPermLevel).collect(Collectors.toList());
        assertEquals(intValues, afterSort);
    }

    @Test
    public void testStringLengthComparator() {
        List<String> test = new ArrayList<>();
        test.add(RandomData.randomString(10));
        test.add(RandomData.randomString(3));
        test.add(RandomData.randomString(20));
        test.add(RandomData.randomString(7));
        test.add(RandomData.randomString(4));
        test.add(RandomData.randomString(86));
        test.add(RandomData.randomString(6));

        test.sort(new StringLengthComparator());

        assertEquals(86, test.get(0).length());
        assertEquals(20, test.get(1).length());
        assertEquals(3, test.get(test.size() - 1).length());
    }

    @Test
    public void testLocaleEntryComparator() {
        Map<Msg, Message> test = new HashMap<>();
        test.put(Msg.CMD_CONSTANT_FOOTER, new Message(""));
        test.put(Msg.ANALYSIS_3RD_PARTY, new Message(""));
        test.put(Msg.MANAGE_FAIL_NO_PLAYERS, new Message(""));

        List<String> sorted = test.entrySet().stream()
                .sorted(new LocaleEntryComparator())
                .map(entry -> entry.getKey().name())
                .collect(Collectors.toList());

        assertEquals("ANALYSIS_3RD_PARTY", sorted.get(0));
        assertEquals("CMD_CONSTANT_FOOTER", sorted.get(1));
        assertEquals("MANAGE_FAIL_NO_PLAYERS", sorted.get(2));
    }
}
