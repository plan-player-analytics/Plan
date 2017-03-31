/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import java.util.HashMap;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Risto
 */
public class HtmlUtilsTest {
    
    public HtmlUtilsTest() {
    }

    @Ignore("Mock JavaPlugin") @Test
    public void testGetHtmlStringFromResource() throws Exception {
        System.out.println("getHtmlStringFromResource");
        String fileName = "";
        String expResult = "";
        String result = HtmlUtils.getHtmlStringFromResource(fileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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

    @Ignore("Mock JavaPlugin") @Test
    public void testGetServerAnalysisUrl() {
        System.out.println("getServerAnalysisUrl");
        String expResult = "";
        String result = HtmlUtils.getServerAnalysisUrl();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    @Ignore("Mock JavaPlugin") @Test
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
