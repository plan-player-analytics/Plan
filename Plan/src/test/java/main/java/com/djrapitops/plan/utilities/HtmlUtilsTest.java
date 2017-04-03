/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import java.io.FileNotFoundException;
import java.util.HashMap;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HtmlUtilsTest {
    
    public HtmlUtilsTest() {
    }

    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        Plan plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }
    
    @Test
    public void testGetHtmlStringFromResource() throws Exception {
        System.out.println("getHtmlStringFromResource");
        String fileName = "player.html";
        String result = HtmlUtils.getHtmlStringFromResource(fileName);
        assertTrue("Result null", result != null);
        assertTrue("Result empty", !result.isEmpty());
    }

    @Test
    public void testReplacePlaceholders() {
        String html = "%test%";
        HashMap<String, String> replaceMap = new HashMap<>();
        replaceMap.put("%test%", "Success");
        String expResult = "Success";
        String result = HtmlUtils.replacePlaceholders(html, replaceMap);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testReplacePlaceholdersBackslash() {
        HashMap<String, String> replace = new HashMap<>();
        replace.put("%test%", "/\\");
        String result = HtmlUtils.replacePlaceholders("%test% alright %test%", replace);
        String exp = "/\\ alright /\\";
        assertEquals(result, exp);
    }

    @Ignore("Mock Server.getIp") @Test
    public void testGetServerAnalysisUrl() throws FileNotFoundException {
        String result = HtmlUtils.getServerAnalysisUrl();
        String exp = "";
        assertEquals(result, exp);
    }

    @Ignore("Mock Server.getIp") @Test
    public void testGetInspectUrl() {
        String playerName = "";
        String expResult = "";
        String result = HtmlUtils.getInspectUrl(playerName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Test
    public void testRemoveXSS() {
        String xss = "<script></script><!--";
        boolean passed = HtmlUtils.removeXSS(xss).length() < xss.length();
        assertEquals(true, passed);
    }    
}
