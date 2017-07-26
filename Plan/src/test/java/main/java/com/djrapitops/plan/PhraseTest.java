package test.java.main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.Phrase;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

import static org.junit.Assert.assertEquals;

/**
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class PhraseTest {

    /**
     *
     */
    public PhraseTest() {
    }

    /**
     *
     */
    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
    }

    /**
     *
     */
    @Test
    public void testToString() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "REPLACE0";
        instance.setText(expResult);
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testParse_0args() {
        Phrase instance = Phrase.DEM_UNKNOWN;
        String expResult = "Not Known";
        String result = instance.parse();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testParse_StringArr() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "Test";
        String result = instance.parse(expResult);
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testColor() {
        Phrase instance = Phrase.COLOR_MAIN;
        ChatColor expResult = ChatColor.RED;
        instance.setColor('c');
        ChatColor result = instance.color();
        assertEquals(expResult, result);
    }

    /**
     *
     */
    @Test
    public void testSetText() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "Test";
        instance.setText(expResult);
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
