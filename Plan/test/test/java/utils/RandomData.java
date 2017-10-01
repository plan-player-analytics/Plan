package test.java.utils;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserInfo;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {

    private static final Random r = new Random();

    public static int randomInt(int rangeStart, int rangeEnd) {
        return ThreadLocalRandom.current().nextInt(rangeStart, rangeEnd);
    }

    public static String randomString(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

    public static List<WebUser> randomWebUsers() throws PassEncryptUtil.CannotPerformOperationException {
        List<WebUser> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new WebUser(randomString(5), PassEncryptUtil.createHash(randomString(7)), r.nextInt()));
        }
        return test;
    }

    public static List<TPS> randomTPS() {
        List<TPS> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int randInt = r.nextInt();
            long randLong = r.nextLong();
            test.add(new TPS(randLong, randLong, randInt, randLong, randLong, randInt, randInt));
        }
        return test;
    }

    public static List<Session> randomSessions() {
        List<Session> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new Session(1, r.nextLong(), r.nextLong(), 0, 0));
        }
        return test;
    }

    public static List<Point> randomPoints() {
        List<Point> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new Point(r.nextLong(), r.nextLong()));
        }
        return test;
    }

    public static <T extends Enum> T randomEnum(Class<T> clazz) {
        int x = r.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }

    public static List<UserInfo> randomUserData() {
        List<UserInfo> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            UserInfo info = new UserInfo(UUID.randomUUID(), randomString(10), r.nextLong(), r.nextBoolean(), r.nextBoolean());
            info.setLastSeen(r.nextLong());
            test.add(info);
        }
        return test;
    }
}
