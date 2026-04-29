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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;
import com.djrapitops.plan.gathering.domain.*;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.sql.tables.KillsTable;
import com.djrapitops.plan.utilities.comparators.GeoInfoComparator;
import org.apache.commons.text.RandomStringGenerator;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomData {

    private static final Random r = new Random();
    private static final int JOIN_ADDRESS_COUNT = 50;
    private static final List<JoinAddress> JOIN_ADDRESSES = generateJoinAddresses(JOIN_ADDRESS_COUNT);

    private RandomData() {
        /* Static method class */
    }

    public static int randomInt(int rangeStart, int rangeEnd) {
        return ThreadLocalRandom.current().nextInt(rangeStart, rangeEnd);
    }

    public static long randomTime() {
        return randomTimeAfter(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(60L));
    }

    public static long randomTimeAfter(long after) {
        return randomLong(after, System.currentTimeMillis());
    }

    public static long randomLong(long rangeStart, long rangeEnd) {
        return ThreadLocalRandom.current().nextLong(rangeStart, rangeEnd);
    }

    public static String randomString(int size) {
        return new RandomStringGenerator.Builder().withinRange('A', 'z').get()
                .generate(size)
                .replace(';', '_')
                .replace('\\', '_');
    }

    public static List<Nickname> randomNicknames(ServerUUID serverUUID) {
        return pickMultiple(randomInt(15, 30), () -> randomNickname(serverUUID));
    }

    public static Nickname randomNickname(ServerUUID serverUUID) {
        return new Nickname(randomString(randomInt(50, 100)), randomTime(), serverUUID);
    }

    public static List<TPS> randomTPS() {
        List<TPS> test = new ArrayList<>();
        for (int i = 0; i < randomInt(5, 100); i++) {
            long randDate = Math.abs(r.nextLong());
            int randPlayers = r.nextInt(10000);
            double randTPS = r.nextDouble() % 20.0;
            double randCPU = r.nextDouble() % 100.0;
            long randMemory = r.nextLong() % 100000L;
            int randEntities = r.nextInt(10000);
            int randChunks = r.nextInt(10000);
            long randDisk = r.nextLong() % 100000L;
            test.add(new TPS(randDate, randTPS, randPlayers, randCPU, randMemory, randEntities, randChunks, randDisk));
        }
        return test;
    }

    public static List<TPS> randomDateOrderedTPS() {
        return randomDateOrderedTPS(50);
    }

    public static List<TPS> randomDateOrderedTPS(long minimumDay) {
        List<TPS> test = new ArrayList<>();
        long previousTimestamp = randomLong(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(minimumDay), System.currentTimeMillis());
        int previousPlayers = randomInt(0, 100);
        for (int i = 0; i < randomInt(50, 100); i++) {
            int randInt = r.nextInt();
            double randTps = r.nextDouble() * 20;
            long randLong = Math.abs(r.nextLong());
            test.add(new TPS(previousTimestamp, randTps, previousPlayers, randLong, randLong, randInt, randInt, randLong));
            boolean reboot = Math.random() < 0.10;
            previousTimestamp = previousTimestamp + (reboot ? TimeUnit.MINUTES.toMillis(1) : TimeUnit.MINUTES.toMillis(10));
            previousPlayers = Math.max(previousPlayers + r.nextInt(10) - 10, 0);
        }
        return test;
    }

    public static List<FinishedSession> randomSessions() {
        return pickMultiple(randomInt(15, 30),
                () -> randomSession(
                        TestConstants.SERVER_UUID,
                        pickMultiple(4, () -> randomString(5)).toArray(new String[0]),
                        pickMultiple(5, UUID::randomUUID).toArray(new UUID[0])
                )
        );
    }

    public static String randomGameMode() {
        return pickAtRandom(GMTimes.getGMKeyArray());
    }

    public static <T> T pickAtRandom(T[] from) {
        return from[randomInt(0, from.length)];
    }

    public static <T> List<T> pickMultiple(int howMany, Supplier<T> supplier) {
        List<T> picked = new ArrayList<>();
        for (int i = 0; i < howMany; i++) {
            picked.add(supplier.get());
        }
        return picked;
    }

    public static List<FinishedSession> randomSessions(ServerUUID serverUUID, String[] worlds, UUID... uuids) {
        return pickMultiple(randomInt(5, 50), () -> randomSession(serverUUID, worlds, uuids));
    }

    public static FinishedSession randomSession(ServerUUID serverUUID, String[] worlds, UUID... uuids) {
        long start = RandomData.randomTime();
        return randomSession(serverUUID, worlds, start, uuids);
    }

    public static FinishedSession randomSession(ServerUUID serverUUID, String[] worlds, long start, UUID... uuids) {
        DataMap extraData = new DataMap();
        extraData.put(WorldTimes.class, RandomData.randomWorldTimes(worlds));
        long end = RandomData.randomTimeAfter(start + 1);

        if (uuids.length >= 2) {
            List<PlayerKill> kills = RandomData.randomKills(serverUUID, uuids[0], pickAtRandom(Arrays.copyOfRange(uuids, 1, uuids.length)));
            extraData.put(PlayerKills.class, new PlayerKills(kills));
        }
        extraData.put(MobKillCounter.class, new MobKillCounter());
        extraData.put(DeathCounter.class, new DeathCounter());
        extraData.put(JoinAddress.class, JOIN_ADDRESSES.get(randomInt(0, JOIN_ADDRESS_COUNT)));
        return new FinishedSession(
                uuids[0], serverUUID,
                start, end, RandomData.randomLong(0, end - start),
                extraData
        );
    }

    public static List<ActiveSession> randomUnfinishedSessions(ServerUUID serverUUID, String[] worlds, UUID... uuids) {
        return pickMultiple(randomInt(5, 50), () -> randomUnfinishedSession(serverUUID, worlds, uuids));
    }

    public static ActiveSession randomUnfinishedSession(ServerUUID serverUUID, String[] worlds, UUID... uuids) {
        ActiveSession session = new ActiveSession(uuids[0], serverUUID, RandomData.randomTime(), pickAtRandom(worlds), randomGameMode());

        session.getExtraData().get(WorldTimes.class)
                .ifPresent(worldTimes -> worldTimes.setAll(RandomData.randomWorldTimes(worlds)));

        if (uuids.length >= 2) {
            List<PlayerKill> randomKills = RandomData.randomKills(serverUUID, uuids[0], pickAtRandom(Arrays.copyOfRange(uuids, 1, uuids.length)));
            session.getExtraData().get(PlayerKills.class).ifPresent(kills -> kills.addAll(randomKills));
        }

        return session;
    }

    public static List<Point> randomPoints() {
        List<Point> test = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            test.add(new Point(r.nextLong(), r.nextLong()));
        }
        return test;
    }

    public static List<GeoInfo> randomGeoInfo() {
        List<GeoInfo> geoInfos = pickMultiple(randomInt(15, 30), () -> new GeoInfo(randomString(10), randomTime()));
        geoInfos.sort(new GeoInfoComparator());
        return geoInfos;
    }

    public static WorldTimes randomWorldTimes(String... worlds) {
        Map<String, GMTimes> times = new HashMap<>();
        for (String world : worlds) {
            Map<String, Long> gmTimes = new HashMap<>();
            for (String gm : GMTimes.getGMKeyArray()) {
                gmTimes.put(gm, randomLong(0, TimeUnit.HOURS.toMillis(2)));
            }
            times.put(world, new GMTimes(gmTimes));
        }
        return new WorldTimes(times);
    }

    public static List<PlayerKill> randomKills(ServerUUID serverUUID, UUID killer, UUID... victimUUIDs) {
        if (victimUUIDs == null || victimUUIDs.length == 1 && victimUUIDs[0] == null) return Collections.emptyList();

        return pickMultiple(randomInt(3, 15), () -> TestData.getPlayerKill(
                killer, pickAtRandom(victimUUIDs),
                serverUUID, randomString(randomInt(10, KillsTable.WEAPON_COLUMN_LENGTH)),
                randomTime()
        ));
    }

    public static List<Ping> randomPings(ServerUUID serverUUID) {
        return pickMultiple(randomInt(15, 30), () -> randomPing(serverUUID));
    }

    public static Ping randomPing(ServerUUID serverUUID) {
        int r1 = randomInt(1, 200);
        int r2 = randomInt(1, 200);
        return new Ping(randomTime(), serverUUID, Math.min(r1, r2), Math.max(r1, r2), (r1 + r2) / 2.0);
    }

    public static List<DateObj<Integer>> randomIntDateObjects() {
        return pickMultiple(randomInt(15, 30), RandomData::randomIntDateObject);
    }

    public static DateObj<Integer> randomIntDateObject() {
        return new DateObj<>(randomTime(), randomInt(0, 500));
    }

    public static DateObj<Integer> randomIntDateObject(int rangeStart, int rangeEnd) {
        return new DateObj<>(randomTime(), randomInt(rangeStart, rangeEnd));
    }

    public static double randomDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static List<JoinAddress> generateJoinAddresses(int n) {
        return IntStream.range(0, n).mapToObj(i -> "join_address_" + i)
                .map(JoinAddress::new)
                .collect(Collectors.toList());
    }

    public static List<UUID> randomUUIDs(int n) {
        return IntStream.range(0, n).mapToObj(i -> UUID.randomUUID()).toList();
    }
}
