package test.java.utils;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.WebUser;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.data.handling.info.InfoType;
import main.java.com.djrapitops.plan.database.tables.GMTimesTable;
import main.java.com.djrapitops.plan.utilities.PassEncryptUtil;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import org.apache.commons.lang.RandomStringUtils;

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

    public static List<UserData> randomUserData() {
        List<UserData> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String randomName = randomString(10);
            UserData uD = new UserData(UUID.randomUUID(), r.nextLong(), r.nextBoolean(), GMTimesTable.getGMKeyArray()[r.nextInt(3)], randomName, r.nextBoolean());
            uD.setLastPlayed(r.nextLong());
            test.add(uD);
        }
        return test;
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

    public static List<SessionData> randomSessions() {
        List<SessionData> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new SessionData(r.nextLong(), r.nextLong()));
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

    public static List<HandlingInfo> randomHandlingInfo() {
        List<HandlingInfo> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new HandlingInfo(UUID.randomUUID(), randomEnum(InfoType.class), r.nextLong()) {
                @Override
                public boolean process(UserData uData) {
                    return false;
                }
            });
        }
        return test;
    }

    public static <T extends Enum> T randomEnum(Class<T> clazz) {
        int x = r.nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}
