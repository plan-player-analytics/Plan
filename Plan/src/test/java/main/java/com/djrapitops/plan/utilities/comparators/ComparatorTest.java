package test.java.main.java.com.djrapitops.plan.utilities.comparators;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import main.java.com.djrapitops.plan.utilities.comparators.*;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ComparatorTest {

    private Random r = new Random();

    @Test
    public void testHandlingInfoComparator() {
        List<HandlingInfo> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new HandlingInfo(null, null, r.nextLong()) {
                @Override
                public boolean process(UserData uData) {
                    return false;
                }
            });
        }
        List<Long> longValues = test.stream().map(HandlingInfo::getTime).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new HandlingInfoTimeComparator());
        List<Long> afterSort = test.stream().map(HandlingInfo::getTime).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testPointComparator() {
        List<Point> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new Point(r.nextLong(), r.nextLong()));
        }

        List<Long> longValues = test.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new PointComparator());
        List<Long> afterSort = test.stream().map(Point::getX).map(i -> (long) (double) i).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testSessionDataComparator() {
        List<SessionData> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new SessionData(r.nextLong(), r.nextLong()));
        }
        List<Long> longValues = test.stream().map(SessionData::getSessionStart).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new SessionDataComparator());
        List<Long> afterSort = test.stream().map(SessionData::getSessionStart).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testTPSComparator() {
        List<TPS> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new TPS(r.nextLong(), 0, 0, 0, 0, 0, 0));
        }
        List<Long> longValues = test.stream().map(TPS::getDate).collect(Collectors.toList());
        longValues.sort(Long::compare);
        test.sort(new TPSComparator());
        List<Long> afterSort = test.stream().map(TPS::getDate).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testUserDataLastPlayedComparator() {
        List<UserData> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            UserData uD = new UserData(null, 0, false, "", "", false);
            uD.setLastPlayed(r.nextLong());
            test.add(uD);
        }
        List<Long> longValues = test.stream().map(UserData::getLastPlayed).collect(Collectors.toList());
        longValues.sort(Long::compare);
        Collections.reverse(longValues);
        test.sort(new UserDataLastPlayedComparator());
        List<Long> afterSort = test.stream().map(UserData::getLastPlayed).collect(Collectors.toList());
        assertEquals(longValues, afterSort);
    }

    @Test
    public void testUserDataNameComparator() {
        List<UserData> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String randomName = UUID.randomUUID().toString().split("-")[0];
            test.add(new UserData(null, 0, false, "", randomName, false));
        }
        List<String> stringValues = test.stream().map(UserData::getName).collect(Collectors.toList());
        Collections.sort(stringValues);
        test.sort(new UserDataNameComparator());
        List<String> afterSort = test.stream().map(UserData::getName).collect(Collectors.toList());
        assertEquals(stringValues, afterSort);
    }

    @Test
    public void testWebUserComparator() {
        List<WebUser> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new WebUser("", "", r.nextInt()));
        }
        List<Integer> intValues = test.stream().map(WebUser::getPermLevel).collect(Collectors.toList());
        intValues.sort(Integer::compare);
        Collections.reverse(intValues);
        test.sort(new WebUserComparator());
        List<Integer> afterSort = test.stream().map(WebUser::getPermLevel).collect(Collectors.toList());
        assertEquals(intValues, afterSort);
    }
}
