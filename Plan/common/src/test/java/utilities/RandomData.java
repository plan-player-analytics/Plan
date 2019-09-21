/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package utilities;

import com.djrapitops.plan.delivery.domain.WebUser;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.gathering.domain.GeoInfo;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.gathering.domain.TPS;
import com.djrapitops.plan.gathering.domain.UserInfo;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {

    private RandomData() {
        /* Static method class */
    }

    private static final Random r = new Random();

    public static int randomInt(int rangeStart, int rangeEnd) {
        return ThreadLocalRandom.current().nextInt(rangeStart, rangeEnd);
    }

    public static long randomLong(long rangeStart, long rangeEnd) {
        return ThreadLocalRandom.current().nextLong(rangeStart, rangeEnd);
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
            long randLong = Math.abs(r.nextLong());
            test.add(new TPS(randLong, randLong, randInt, randLong, randLong, randInt, randInt, randLong));
        }
        return test;
    }

    public static List<Session> randomSessions() {
        List<Session> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(randomSession());
        }
        return test;
    }

    public static Session randomSession() {
        return new Session(1, TestConstants.PLAYER_ONE_UUID, TestConstants.SERVER_UUID, r.nextLong(), r.nextLong(), 0, 0, 0);
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
            UserInfo info = new UserInfo(UUID.randomUUID(), UUID.randomUUID(), r.nextLong(), r.nextBoolean(), r.nextBoolean());
            test.add(info);
        }
        return test;
    }

    public static List<GeoInfo> randomGeoInfo() {
        List<GeoInfo> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            GeoInfo geoInfo = new GeoInfo(randomString(10), r.nextLong());
            test.add(geoInfo);
        }
        return test;
    }
}
