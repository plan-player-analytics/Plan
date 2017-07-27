/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.utilities.comparators;

import com.djrapitops.plugin.utilities.player.Gamemode;
import main.java.com.djrapitops.plan.data.handling.info.GamemodeInfo;
import main.java.com.djrapitops.plan.data.handling.info.HandlingInfo;
import main.java.com.djrapitops.plan.utilities.comparators.HandlingInfoTimeComparator;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
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
        GamemodeInfo one = new GamemodeInfo(null, 500L, Gamemode.CREATIVE);
        i.add(one);
        GamemodeInfo two = new GamemodeInfo(null, 400L, Gamemode.CREATIVE);
        i.add(two);
        GamemodeInfo three = new GamemodeInfo(null, 100L, Gamemode.CREATIVE);
        i.add(three);
        GamemodeInfo four = new GamemodeInfo(null, 700L, Gamemode.CREATIVE);
        i.add(four);
        i.sort(new HandlingInfoTimeComparator());
        assertEquals(three, i.get(0));
        assertEquals(two, i.get(1));
        assertEquals(one, i.get(2));
        assertEquals(four, i.get(3));

    }

}
