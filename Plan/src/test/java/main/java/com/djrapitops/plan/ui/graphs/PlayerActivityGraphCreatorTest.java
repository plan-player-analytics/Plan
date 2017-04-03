/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.main.java.com.djrapitops.plan.ui.graphs;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.ui.graphs.PlayerActivityGraphCreator;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Risto
 */
public class PlayerActivityGraphCreatorTest {
    
    public PlayerActivityGraphCreatorTest() {
    }

    @Test
    public void testGenerateDataArray() {
        List<SessionData> sessionData = createRandomSessionDataList();
        long scale = 2592000L * 1000L;
        String result = PlayerActivityGraphCreator.generateDataArray(sessionData, scale, 20)[1];
        assertTrue("0", 0 < result.length());
    }
    
    public static List<SessionData> createRandomSessionDataList() {
        List<SessionData> list = new ArrayList<>();
        Random r = new Random();
        long now = new Date().toInstant().getEpochSecond();
        while (list.size() < 500) {
            int randomStart = r.nextInt(2592000);
            long start = now - (long) (randomStart+10);
            long end = start + (long) r.nextInt(randomStart);
            list.add(new SessionData((start * (long) 1000), (end * (long) 1000)));
        }
        return list;
    }
}
