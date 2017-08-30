/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.data.additional.importer;

import com.google.common.collect.ImmutableMap;
import main.java.com.djrapitops.plan.data.PlayerKill;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.systems.processing.importing.ServerImportData;
import main.java.com.djrapitops.plan.systems.processing.importing.UserImportData;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * @author Fuzzlemann
 */
public class ImportBuilderTest {

    private int randomInt = RandomData.randomInt(0, 10);
    private String randomString = RandomData.randomString(randomInt);

    @Test
    public void testEmptyServerBuilder() {
        ServerImportData data = ServerImportData.builder().build();

        assertNotNull(data.getCommandUsages());
        assertNotNull(data.getTpsData());
    }

    @Test
    public void testEmptyUserBuilder() {
        UserImportData data = UserImportData.builder().build();

        assertEquals(0, data.getRegistered());
        assertEquals(0, data.getTimesKicked());
        assertEquals(0, data.getMobKills());
        assertEquals(0, data.getDeaths());

        assertNull(data.getName());
        assertNull(data.getUuid());

        assertFalse(data.isOp());
        assertFalse(data.isBanned());

        assertNotNull(data.getWorldTimes());
        assertNotNull(data.getNicknames());
        assertNotNull(data.getKills());
        assertNotNull(data.getIps());

        assertTrue(data.getWorldTimes().isEmpty());
        assertTrue(data.getNicknames().isEmpty());
        assertTrue(data.getKills().isEmpty());
        assertTrue(data.getIps().isEmpty());
    }

    @Test
    public void testServerDataBuilder() {
        ServerImportData.ServerImportDataBuilder builder = ServerImportData.builder();

        TPS tps = new TPS(randomInt, randomInt, randomInt, randomInt, randomInt, randomInt, randomInt);

        ServerImportData data = builder.tpsData(tps)
                .tpsData(tps)
                .tpsData(tps)
                .tpsData(tps, tps, tps)
                .tpsData(Collections.emptyList())
                .tpsData(randomInt, randomInt, randomInt, randomInt, randomInt, randomInt, randomInt)
                .tpsData(Collections.singletonList(tps))
                .tpsData(Arrays.asList(tps, tps))
                .commandUsage(randomString, randomInt)
                .commandUsage(randomString, randomInt)
                .commandUsage(randomString, randomInt)
                .commandUsage(randomString, randomInt)
                .commandUsages(new HashMap<>())
                .commandUsages(ImmutableMap.of(randomString, randomInt))
                .build();

        assertEquals(10, data.getTpsData().size());
        assertEquals(1, data.getCommandUsages().size());

        assertEquals(randomInt, data.getTpsData().get(0).getDate());
    }

    @Test
    public void testUserDataBuilder() {
        UserImportData.UserImportDataBuilder builder = UserImportData.builder();

        UUID uuid = UUID.randomUUID();
        PlayerKill playerKill = new PlayerKill(uuid, randomString, 1);
        GMTimes gmTimes = new GMTimes(randomString, randomInt);

        UserImportData data = builder.uuid(uuid)
                .banned()
                .banned(false)
                .op()
                .ips(randomString, randomString)
                .ips(Collections.singletonList(randomString))
                .kills(playerKill, playerKill, playerKill)
                .kills(Collections.singleton(playerKill))
                .name(randomString)
                .registered(randomInt)
                .timesKicked(randomInt)
                .mobKills(randomInt)
                .worldTimes(randomString, randomInt, randomInt, randomInt, randomInt)
                .worldTimes(randomString, gmTimes)
                .deaths(randomInt)
                .worldTimes(ImmutableMap.of(randomString, gmTimes))
                .nicknames(randomString, randomString)
                .nicknames(Collections.singletonList(randomString))
                .build();

        assertNotNull(data);

        assertFalse(data.isBanned());
        assertTrue(data.isOp());

        assertEquals(randomInt, data.getDeaths());
        assertEquals(1, data.getWorldTimes().size());
        assertEquals(3, data.getIps().size());
        assertEquals(playerKill, data.getKills().get(0));
        assertEquals(randomInt, data.getMobKills());
        assertEquals(3, data.getNicknames().size());
        assertEquals(randomInt, data.getTimesKicked());

        assertEquals(uuid.toString(), data.getUuid());
        assertEquals(randomString, data.getName());
    }
}
