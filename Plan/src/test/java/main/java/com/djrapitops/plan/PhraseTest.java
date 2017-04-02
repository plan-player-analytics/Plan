/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan;

import main.java.com.djrapitops.plan.Phrase;
import org.bukkit.ChatColor;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Risto
 */
public class PhraseTest {
    
    public PhraseTest() {
    }

    @Ignore@Test
    public void testToString() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "REPLACE0";
        instance.setText(expResult);        
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    @Ignore@Test
    public void testParse_0args() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "REPLACE0";
        String result = instance.parse();
        assertEquals(expResult, result);
    }

    @Ignore@Test
    public void testParse_StringArr() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "Test";
        String result = instance.parse(expResult);
        assertEquals(expResult, result);
    }

    @Ignore@Test
    public void testColor() {
        Phrase instance = Phrase.COLOR_MAIN;
        ChatColor expResult = ChatColor.RED;
        instance.setColor('c');
        ChatColor result = instance.color();
        assertEquals(expResult, result);
    }

    @Ignore@Test
    public void testSetText() {
        Phrase instance = Phrase.REPLACE0;
        String expResult = "Test";
        instance.setText(expResult);
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
