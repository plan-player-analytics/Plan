/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.utilities.ManageUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class ManageUtilsTest {

    private int threshold = 5000;

    /**
     *
     */
    public ManageUtilsTest() {
    }

    /**
     *
     * @throws IOException
     * @throws Exception
     */
    @Before
    public void setUp() throws IOException, Exception {
        TestInit t = TestInit.init();
        assertTrue("Not set up", t.setUp());
    }

    /**
     *
     */
    @Test
    public void testContainsCombinable() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, 100));
        data.add(new SessionData(threshold - 100, threshold * 2));
        data.add(new SessionData(threshold * 2 + 100, threshold * 3));
        assertTrue(ManageUtils.containsCombinable(data));
    }

    /**
     *
     */
    @Test
    public void testContainsCombinableFalse() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, threshold));
        data.add(new SessionData(threshold * 3, threshold * 4));
        assertTrue(!ManageUtils.containsCombinable(data));
    }

    /**
     *
     */
    @Test
    public void testContainsCombinableFalse2() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, threshold * 2));
        data.add(new SessionData(threshold * 3, threshold * 4));
        assertTrue(!ManageUtils.containsCombinable(data));
    }

    /**
     *
     */
    @Test
    public void testContainsCombinableFalse3() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, threshold * 2));
        data.add(new SessionData(threshold * 3 + 200, threshold * 4));
        assertTrue(!ManageUtils.containsCombinable(data));
    }

    /**
     *
     */
    @Test
    public void testCombineSessions() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, 100));
        data.add(new SessionData(threshold, threshold * 2));
        data.add(new SessionData(threshold * 2 + 100, threshold * 3));
        SessionData get = ManageUtils.combineSessions(data, 1).get(0);
        SessionData exp = new SessionData(0, threshold * 3);
        assertEquals(exp, get);
    }

    /**
     *
     */
    @Test
    public void testCombineSessions2() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, 100));
        data.add(new SessionData(threshold, threshold * 2));
        data.add(new SessionData(threshold * 2 + 100, threshold * 3));
        data.add(new SessionData(threshold * 3 + 200, threshold * 4));
        SessionData get = ManageUtils.combineSessions(data, 1).get(0);
        SessionData exp = new SessionData(0, threshold * 4);
        assertEquals(exp, get);
    }

    /**
     *
     */
    @Test
    public void testCombineSessions3() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, 100));
        data.add(new SessionData(threshold, threshold * 2));
        data.add(new SessionData(threshold * 3 + 200, threshold * 4));
        List<SessionData> result = ManageUtils.combineSessions(data, 2);
        SessionData exp = new SessionData(0, threshold * 2);
        assertEquals(exp, result.get(0));
        SessionData exp2 = new SessionData(threshold * 3 + 200, threshold * 4);
        assertEquals(exp2, result.get(1));
    }

    /**
     *
     */
    @Test
    public void testCombineSessions4() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, 100));
        data.add(new SessionData(threshold, threshold * 2));
        data.add(new SessionData(threshold * 3 + 200, threshold * 4));
        data.add(new SessionData(threshold * 5 - 200, threshold * 5));
        List<SessionData> result = ManageUtils.combineSessions(data, 2);
        SessionData exp = new SessionData(0, threshold * 2);
        assertEquals(exp, result.get(0));
        SessionData exp2 = new SessionData(threshold * 3 + 200, threshold * 5);
        assertEquals(exp2, result.get(1));
    }

    /**
     *
     */
    @Test
    public void testCombineSessions5() {
        List<SessionData> data = new ArrayList<>();
        data.add(new SessionData(0, 100));
        data.add(new SessionData(threshold, threshold * 2));
        data.add(new SessionData(threshold * 5, threshold * 5 + 100));
        data.add(new SessionData(threshold * 8, threshold * 8 + 200));
        data.add(new SessionData(threshold * 9 - 200, threshold * 10));
        List<SessionData> result = ManageUtils.combineSessions(data, 3);
        SessionData exp = new SessionData(0, threshold * 2);
        assertEquals(exp, result.get(0));
        SessionData exp2 = new SessionData(threshold * 5, threshold * 5 + 100);
        assertEquals(exp2, result.get(1));
        SessionData exp3 = new SessionData(threshold * 8, threshold * 10);
        assertEquals(exp3, result.get(2));
    }

}
