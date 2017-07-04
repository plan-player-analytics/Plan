/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import java.io.FileNotFoundException;
import java.util.HashMap;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import org.bukkit.plugin.java.JavaPlugin;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

/**
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HtmlUtilsTest {

    /**
     *
     */
    public HtmlUtilsTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
    }

    /**
     *
     * @throws Exception
     */
    @Test
    public void testGetHtmlStringFromResource() throws Exception {
        String fileName = "player.html";
        String result = HtmlUtils.getHtmlStringFromResource(fileName);
        assertTrue("Result null", result != null);
        assertTrue("Result empty", !result.isEmpty());
    }

    /**
     *
     */
    @Test
    public void testReplacePlaceholders() {
        String html = "%test%";
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("%test%", "Success");
        String expResult = "Success";
        String result = HtmlUtils.replacePlaceholders(html, replaceMap);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testReplacePlaceholdersBackslash() {
        HashMap<String, String> replace = new HashMap<>();
        replace.put("%test%", "/\\");
        String result = HtmlUtils.replacePlaceholders("%test% alright %test%", replace);
        String exp = "/\\ alright /\\";
        assertEquals(result, exp);
    }

    /**
     *
     * @throws FileNotFoundException
     */
    @Test
    public void testGetServerAnalysisUrl() throws FileNotFoundException {
        String result = HtmlUtils.getServerAnalysisUrlWithProtocol();
        String exp = "http://0.0.0.0:8804/bAkEd/server";
        assertEquals(exp, result);
    }

    /**
     *
     */
    @Test
    public void testGetInspectUrl() {
        String playerName = "Test";
        String expResult = "http://0.0.0.0:8804/bAkEd/player/Test";
        String result = HtmlUtils.getInspectUrlWithProtocol(playerName);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testRemoveXSS() {
        String xss = "<script></script><!--";
        boolean passed = HtmlUtils.removeXSS(xss).length() < xss.length();
        assertEquals(true, passed);
    }
}
