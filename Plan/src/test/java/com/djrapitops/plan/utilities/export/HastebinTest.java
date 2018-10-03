package com.djrapitops.plan.utilities.export;

import com.djrapitops.plan.utilities.file.export.Hastebin;
import com.google.common.collect.Iterables;
import org.junit.Test;
import utilities.RandomData;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Fuzzlemann
 */
public class HastebinTest {

    @Test
    public void testSplitting() {
        Iterable<String> parts = Hastebin.split(RandomData.randomString(500000));

        int expPartCount = 2;
        int partCount = Iterables.size(parts);

        assertEquals(expPartCount, partCount);
    }
}
