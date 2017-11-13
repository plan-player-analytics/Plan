package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.api.TimeAmount;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.MockUtils;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class FormatUtilsTest {

    @Before
    public void setUp() throws Exception {
        TestInit.init();
    }

    @Test
    public void testFormatTimeAmount() {
        String expResult = "1s";
        String result = FormatUtils.formatTimeAmount(TimeAmount.SECOND.ms());

        assertEquals(expResult, result);
    }

    @Test
    public void testFormatTimeAmountSinceDate() {
        Date before = new Date(300000L);
        Date now = new Date(310000L);

        String expResult = "10s";
        String result = FormatUtils.formatTimeAmountDifference(before.getTime(), now.getTime());

        assertEquals(expResult, result);
    }

    @Test
    public void testFormatTimeStamp() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.ENGLISH);

        Date date = new Date();
        date.setTime(0);

        String expResult = dateFormat.format(date);

        long epochZero = 0L;
        String result = FormatUtils.formatTimeStamp(epochZero);

        assertEquals(expResult, result);
    }

    @Test
    public void testFormatTimeStampYear() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd YYYY, HH:mm", Locale.ENGLISH);

        Date date = new Date();
        date.setTime(0);

        String expResult = dateFormat.format(date);

        long epochZero = 0L;
        String result = FormatUtils.formatTimeStampYear(epochZero);

        assertEquals(expResult, result);
    }

    @Test
    public void testFormatTimeStampSecond() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.ENGLISH);

        Date date = new Date();
        date.setTime(0);

        String expResult = dateFormat.format(date);

        long epochZero = 0L;
        String result = FormatUtils.formatTimeStampSecond(epochZero);

        assertEquals(expResult, result);
    }

    @Test
    public void testFormatTimeStampMonths() {
        long time = TimeAmount.DAY.ms() * 40L;
        assertEquals("1 month, 10d ", FormatUtils.formatTimeAmount(time));
    }

    @Test
    public void testRemoveLetters() {
        String dataPoint = "435729847jirggu.eiwb¤#¤%¤#";
        String expResult = "435729847.";

        String result = FormatUtils.removeLetters(dataPoint);

        assertEquals(expResult, result);
    }

    @Test
    public void testRemoveNumbers() {
        String dataPoint = "34532453.5 $";
        String expResult = "$";

        String result = FormatUtils.removeNumbers(dataPoint);

        assertEquals(expResult, result);
    }

    @Test
    public void testRemoveNumbers2() {
        String dataPoint = "l43r4545tl43  4.5";
        String expResult = "lrtl";

        String result = FormatUtils.removeNumbers(dataPoint);

        assertEquals(expResult, result);
    }

    @Test
    public void testParseVersionNumber() {
        String versionString = "2.10.2";
        long expResult = 21002000000000000L;

        long result = FormatUtils.parseVersionNumber(versionString);

        assertEquals(expResult, result);
    }

    @Test
    public void testVersionNumber() {
        String versionString = "2.10.2";
        String versionString2 = "2.9.3";

        long result = FormatUtils.parseVersionNumber(versionString);
        long result2 = FormatUtils.parseVersionNumber(versionString2);

        assertTrue("Higher version not higher", result > result2);
    }

    @Test
    public void testMergeArrays() {
        String randomString1 = RandomData.randomString(10);
        String randomString2 = RandomData.randomString(10);
        String randomString3 = RandomData.randomString(10);
        String randomString4 = RandomData.randomString(10);

        String[][] arrays = new String[][]{new String[]{randomString1, randomString2}, new String[]{randomString3, randomString4}};
        String[] expResult = new String[]{randomString1, randomString2, randomString3, randomString4};

        String[] result = FormatUtils.mergeArrays(arrays);

        assertArrayEquals(expResult, result);
    }

    @Test
    public void testFormatLocation() {
        int randomInt = RandomData.randomInt(0, 100);

        World mockWorld = MockUtils.mockWorld();
        Location loc = new Location(mockWorld, randomInt, randomInt, randomInt);

        String expResult = "x " + randomInt + " z " + randomInt + " in World";
        String result = FormatUtils.formatLocation(loc);

        assertEquals(expResult, result);
    }

    @Test
    public void testCutDecimalsWhichIsRoundedDown() throws Exception {
        double d = 0.05234;
        String expResult = "0,05";

        String result = FormatUtils.cutDecimals(d);

        assertEquals(expResult, result);
    }

    @Test
    public void testCutDecimalsWhichIsRoundedUp() throws Exception {
        double d = 0.05634;
        String expResult = "0,06";

        String result = FormatUtils.cutDecimals(d);

        assertEquals(expResult, result);
    }

}
