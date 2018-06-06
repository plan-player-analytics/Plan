package com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plan.data.WebUser;
import com.djrapitops.plan.data.container.GeoInfo;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.container.UserInfo;
import com.djrapitops.plan.system.settings.locale.Message;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.html.graphs.line.Point;
import org.junit.Test;
import utilities.RandomData;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class ComparatorTest {

    @Test
    public void pointComparator() {
        List<Point> points = RandomData.randomPoints();

        List<Long> expected = points.stream().map(Point::getX).map(i -> (long) (double) i)
                .sorted(Long::compare).collect(Collectors.toList());

        points.sort(new PointComparator());

        List<Long> result = points.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    @Test
    public void sessionDataComparator() {
        List<Session> sessions = RandomData.randomSessions();

        List<Long> expected = sessions.stream().map(Session::getSessionStart)
                .sorted(Long::compare).collect(Collectors.toList());
        Collections.reverse(expected);

        sessions.sort(new SessionStartComparator());
        List<Long> result = sessions.stream().map(Session::getSessionStart).collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    public void tpsComparator() {
        List<TPS> tpsList = RandomData.randomTPS();

        List<Long> expected = tpsList.stream().map(TPS::getDate)
                .sorted(Long::compare).collect(Collectors.toList());

        tpsList.sort(new TPSComparator());
        List<Long> result = tpsList.stream().map(TPS::getDate).collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    public void userDataLastPlayedComparator() {
        List<UserInfo> userInfo = RandomData.randomUserData();

        List<Long> expected = userInfo.stream().map(UserInfo::getLastSeen)
                .sorted(Long::compare).collect(Collectors.toList());
        Collections.reverse(expected);

        userInfo.sort(new UserInfoLastPlayedComparator());
        List<Long> result = userInfo.stream().map(UserInfo::getLastSeen).collect(Collectors.toList());
        assertEquals(expected, result);
    }

    @Test
    public void userDataNameComparator() {
        List<UserInfo> userInfo = RandomData.randomUserData();

        List<String> expected = userInfo.stream().map(UserInfo::getName)
                .sorted().collect(Collectors.toList());

        userInfo.sort(new UserInfoNameComparator());
        List<String> result = userInfo.stream().map(UserInfo::getName).collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    public void webUserComparator() throws PassEncryptUtil.CannotPerformOperationException {
        List<WebUser> webUsers = RandomData.randomWebUsers();

        List<Integer> expected = webUsers.stream().map(WebUser::getPermLevel)
                .sorted(Integer::compare).collect(Collectors.toList());
        Collections.reverse(expected);

        webUsers.sort(new WebUserComparator());
        List<Integer> result = webUsers.stream().map(WebUser::getPermLevel).collect(Collectors.toList());

        assertEquals(expected, result);
    }

    @Test
    public void stringLengthComparator() {
        List<Integer> result = Stream.of(
                RandomData.randomString(10),
                RandomData.randomString(3),
                RandomData.randomString(4),
                RandomData.randomString(20),
                RandomData.randomString(7),
                RandomData.randomString(4),
                RandomData.randomString(86),
                RandomData.randomString(6)
        )
                .sorted(new StringLengthComparator())
                .map(String::length)
                .collect(Collectors.toList());

        List<Integer> expected = Arrays.asList(86, 20, 10, 7, 6, 4, 4, 3);
        assertEquals(expected, result);
    }

    @Test
    public void localeEntryComparator() {
        Map<Msg, Message> messageMap = new HashMap<>();
        messageMap.put(Msg.CMD_CONSTANT_FOOTER, new Message(RandomData.randomString(10)));
        messageMap.put(Msg.ANALYSIS_3RD_PARTY, new Message(RandomData.randomString(10)));
        messageMap.put(Msg.MANAGE_FAIL_NO_PLAYERS, new Message(RandomData.randomString(10)));

        List<Msg> result = messageMap.entrySet().stream()
                .sorted(new LocaleEntryComparator())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Msg> expected = Arrays.asList(
                Msg.ANALYSIS_3RD_PARTY,
                Msg.CMD_CONSTANT_FOOTER,
                Msg.MANAGE_FAIL_NO_PLAYERS
        );
        assertEquals(expected, result);
    }

    @Test
    public void geoInfoComparator() {
        List<GeoInfo> geoInfos = RandomData.randomGeoInfo();

        List<Long> expected = geoInfos.stream().map(GeoInfo::getDate)
                .sorted(Long::compare).collect(Collectors.toList());
        Collections.reverse(expected);

        geoInfos.sort(new GeoInfoComparator());
        List<Long> result = geoInfos.stream().map(GeoInfo::getDate).collect(Collectors.toList());
        assertEquals(expected, result);
    }
}
