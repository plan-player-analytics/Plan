package main.java.com.djrapitops.plan.utilities.comparators;

import com.google.common.collect.Ordering;
import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.locale.Message;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ComparatorTest {

    @Test
    public void testPointComparator() {
        List<Point> points = RandomData.randomPoints();

        List<Long> longValues = points.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        longValues.sort(Long::compare);

        points.sort(new PointComparator());

        List<Long> afterSort = points.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testSessionDataComparator() {
        List<Session> sessions = RandomData.randomSessions();

        List<Long> longValues = sessions.stream().map(Session::getSessionStart).collect(Collectors.toList());
        longValues.sort(Long::compare);

        Collections.reverse(longValues);
        sessions.sort(new SessionStartComparator());
        List<Long> afterSort = sessions.stream().map(Session::getSessionStart).collect(Collectors.toList());

        assertEquals(longValues, afterSort);
    }

    @Test
    public void testTPSComparator() {
        List<TPS> tpsList = RandomData.randomTPS();

        List<Long> longValues = tpsList.stream().map(TPS::getDate).collect(Collectors.toList());
        longValues.sort(Long::compare);

        tpsList.sort(new TPSComparator());
        List<Long> afterSort = tpsList.stream().map(TPS::getDate).collect(Collectors.toList());

        assertEquals(longValues, afterSort);
    }

    @Test
    public void testUserDataLastPlayedComparator() {
        List<UserInfo> userInfo = RandomData.randomUserData();

        List<Long> longValues = userInfo.stream().map(UserInfo::getLastSeen).collect(Collectors.toList());
        longValues.sort(Long::compare);

        Collections.reverse(longValues);
        userInfo.sort(new UserInfoLastPlayedComparator());
        List<Long> afterSort = userInfo.stream().map(UserInfo::getLastSeen).collect(Collectors.toList());

        assertEquals(longValues, afterSort);
    }


    @Test
    public void testUserDataNameComparator() {
        List<UserInfo> userInfo = RandomData.randomUserData();

        List<String> stringValues = userInfo.stream().map(UserInfo::getName).collect(Collectors.toList());
        Collections.sort(stringValues);

        userInfo.sort(new UserDataNameComparator());
        List<String> afterSort = userInfo.stream().map(UserInfo::getName).collect(Collectors.toList());

        assertEquals(stringValues, afterSort);
    }

    @Test
    public void testWebUserComparator() throws PassEncryptUtil.CannotPerformOperationException {
        List<WebUser> webUsers = RandomData.randomWebUsers();

        List<Integer> intValues = webUsers.stream().map(WebUser::getPermLevel).collect(Collectors.toList());
        intValues.sort(Integer::compare);
        Collections.reverse(intValues);

        webUsers.sort(new WebUserComparator());
        List<Integer> afterSort = webUsers.stream().map(WebUser::getPermLevel).collect(Collectors.toList());

        assertEquals(intValues, afterSort);
    }

    @Test
    public void testStringLengthComparator() {
        List<String> strings = Ordering.from(new StringLengthComparator())
                .sortedCopy(Arrays.asList(
                        RandomData.randomString(10),
                        RandomData.randomString(3),
                        RandomData.randomString(20),
                        RandomData.randomString(7),
                        RandomData.randomString(4),
                        RandomData.randomString(86),
                        RandomData.randomString(6)));

        assertEquals(86, strings.get(0).length());
        assertEquals(20, strings.get(1).length());
        assertEquals(3, strings.get(strings.size() - 1).length());
    }

    @Test
    public void testLocaleEntryComparator() {
        Map<Msg, Message> messageMap = new HashMap<>();
        messageMap.put(Msg.CMD_CONSTANT_FOOTER, new Message(RandomData.randomString(10)));
        messageMap.put(Msg.ANALYSIS_3RD_PARTY, new Message(RandomData.randomString(10)));
        messageMap.put(Msg.MANAGE_FAIL_NO_PLAYERS, new Message(RandomData.randomString(10)));

        List<String> sorted = messageMap.entrySet().stream()
                .sorted(new LocaleEntryComparator())
                .map(entry -> entry.getKey().name())
                .collect(Collectors.toList());

        assertEquals(Msg.ANALYSIS_3RD_PARTY.name(), sorted.get(0));
        assertEquals(Msg.CMD_CONSTANT_FOOTER.name(), sorted.get(1));
        assertEquals(Msg.MANAGE_FAIL_NO_PLAYERS.name(), sorted.get(2));
    }
}
