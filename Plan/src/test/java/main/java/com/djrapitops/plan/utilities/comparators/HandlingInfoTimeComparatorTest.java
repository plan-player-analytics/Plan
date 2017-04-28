/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities.comparators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import main.java.com.djrapitops.plan.data.handling.info.GamemodeInfo;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.utilities.comparators.HandlingInfoTimeComparator;
import org.bukkit.GameMode;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Rsl1122
 */
public class HandlingInfoTimeComparatorTest {
    
    /**
     *
     */
    public HandlingInfoTimeComparatorTest() {
    }

    /**
     *
     */
    @Test
    public void testCompare() {
        List<HandlingInfo> i = new ArrayList<>();
        GamemodeInfo one = new GamemodeInfo(null, 500L, GameMode.CREATIVE);
        i.add(one);
        GamemodeInfo two = new GamemodeInfo(null, 400L, GameMode.CREATIVE);
        i.add(two);
        GamemodeInfo three = new GamemodeInfo(null, 100L, GameMode.CREATIVE);
        i.add(three);
        GamemodeInfo four = new GamemodeInfo(null, 700L, GameMode.CREATIVE);
        i.add(four);
        Collections.sort(i, new HandlingInfoTimeComparator());
        assertEquals(three, i.get(0));
        assertEquals(two, i.get(1));
        assertEquals(one, i.get(2));
        assertEquals(four, i.get(3));
        
    }
    
}
