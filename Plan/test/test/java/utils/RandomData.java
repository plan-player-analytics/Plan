package test.java.utils;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import org.apache.commons.lang.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {

    private static final Random r = new Random();

    public static int randomInt(int rangeStart, int rangeEnd) {
        return ThreadLocalRandom.current().nextInt(rangeStart, rangeEnd);
    }

    public static String randomString(int size) {
        return RandomStringUtils.random(size);
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
            test.add(new Session(r.nextLong(), r.nextLong(), null, null, 0, 0));
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

    public static List<UserData> randomUserData() {
        return new ArrayList<>();
        // TODO Rewrite
    }
}
