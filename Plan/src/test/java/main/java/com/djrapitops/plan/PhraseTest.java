/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.ChatColor;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.bukkit.plugin.java.JavaPlugin;
import org.easymock.EasyMock;
import org.junit.Before;
import org.powermock.api.easymock.PowerMock;
import test.java.utils.TestInit;

/**
 *
 * @author Risto
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class PhraseTest {
    
    private Plan plan;
    
    public PhraseTest() {
    }
    
    @Before
    public void setUp() {
        TestInit t = new TestInit();
        assertTrue("Not set up", t.setUp());
        plan = t.getPlanMock();
        PowerMock.mockStatic(JavaPlugin.class);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        EasyMock.expect(JavaPlugin.getPlugin(Plan.class)).andReturn(plan);
        PowerMock.replay(JavaPlugin.class);
//        PowerMock.verify(JavaPlugin.class);
    }
    
    @Test
    public void testToString() {        
        Phrase instance = Phrase.REPLACE0;
        String expResult = "REPLACE0";
        instance.setText(expResult);        
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Test
    public void testParse_0args() {
        Phrase instance = Phrase.DEM_UNKNOWN;
        String expResult = "Not Known";
        String result = instance.parse();
        assertEquals(expResult, result);
    }

    @Test
    public void testParse_StringArr() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "Test";
        String result = instance.parse(expResult);
        assertEquals(expResult, result);
    }

    @Test
    public void testColor() {
        Phrase instance = Phrase.COLOR_MAIN;
        ChatColor expResult = ChatColor.RED;
        instance.setColor('c');
        ChatColor result = instance.color();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetText() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "Test";
        instance.setText(expResult);
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
