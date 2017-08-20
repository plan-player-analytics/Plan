/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities;

import com.google.common.collect.ImmutableMap;
import main.java.com.djrapitops.plan.utilities.HtmlUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.io.Serializable;
import java.util.Map;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HtmlUtilsTest {

    @Test
    public void testGetHtmlStringFromResource() throws Exception {
        TestInit.init();

        String fileName = "player.html";
        String result = HtmlUtils.getStringFromResource(fileName);

        assertFalse("Result empty", result.isEmpty());
    }

    @Test
    public void testReplacePlaceholders() {
        String randomString = RandomData.randomString(100);
        String randomIdentifier = RandomData.randomString(5);

        String html = "${" + randomIdentifier + "}" + randomString;

        Map<String, Serializable> replaceMap = ImmutableMap.of(randomIdentifier, "Success");

        String expResult = "Success" + randomString;
        String result = HtmlUtils.replacePlaceholders(html, replaceMap);

        assertEquals(expResult, result);
    }

    @Test
    public void testReplacePlaceholdersBackslash() {
        String randomIdentifier = RandomData.randomString(5);

        Map<String, Serializable> replace = ImmutableMap.of(randomIdentifier, "/\\");

        String expResult = "/\\ alright /\\";
        String result = HtmlUtils.replacePlaceholders("${" + randomIdentifier + "} alright ${" + randomIdentifier + "}", replace);

        assertEquals(result, expResult);
    }

    @Test
    public void testRemoveXSS() {
        String randomString = RandomData.randomString(10);

        String xss = "<script>" + randomString + "</script><!--";
        String result = HtmlUtils.removeXSS(xss);

        assertEquals(randomString, result);
    }
}
