/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.ui.graphs;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import main.java.com.djrapitops.plan.utilities.html.graphs.*;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.RandomData;
import test.java.utils.TestInit;

import java.util.*;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Fuzzlemann
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({JavaPlugin.class})
public class GraphTest {

    private final List<TPS> tpsList = new ArrayList<>();
    private final List<Session> sessionList = new ArrayList<>();
    private final Map<String, Integer> geoList = new HashMap<>();
    private final WorldTimes worldTimes = new WorldTimes("WORLD", "SURVIVAL");

    private List<Point> points = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        for (int i = 0; i < 10; i++) {
            tpsList.add(new TPS(i, i, i, i, i, i, i));
            sessionList.add(new Session(i, (long) i, (long) i, i, i));
            geoList.put(String.valueOf(i), i);
        }

        points = RandomData.randomPoints();
    }

    @Test
    public void testGraphCreators() {
        String expected = "[[0,0.0],[1,1.0],[2,2.0],[3,3.0],[4,4.0],[5,5.0],[6,6.0],[7,7.0],[8,8.0],[9,9.0]]";
        assertEquals(expected, CPUGraphCreator.buildSeriesDataString(tpsList));
        assertEquals(expected, PlayerActivityGraphCreator.buildSeriesDataString(tpsList));
        // TODO Fix TimeZone Dependency of this test
        // assertEquals("[{x:3600000, y:3, z:14, marker: { radius:14}},]", PunchCardGraphCreator.createDataSeries(sessionList));

        assertEquals(expected, RamGraphCreator.buildSeriesDataString(tpsList));
        assertEquals(expected, TPSGraphCreator.buildSeriesDataString(tpsList));
        assertEquals(expected, WorldLoadGraphCreator.buildSeriesDataStringChunks(tpsList));
        assertEquals(expected, WorldLoadGraphCreator.buildSeriesDataStringEntities(tpsList));
        assertEquals("[{'code':'1','value':1},{'code':'2','value':2},{'code':'3','value':3},{'code':'4','value':4},{'code':'5','value':5},{'code':'6','value':6},{'code':'7','value':7},{'code':'8','value':8},{'code':'9','value':9}]", WorldMapCreator.createDataSeries(geoList));
        assertEquals("[[{name:'WORLD',y:0,drilldown: 'WORLD'}], [{name:'WORLD', id:'WORLD',colors: gmPieColors,data: [['SURVIVAL',0],['SPECTATOR',0],['CREATIVE',0],['ADVENTURE',0]]}]]",
                Arrays.toString(WorldPieCreator.createSeriesData(worldTimes)));
    }

    @Test
    public void testGraphCreatorsForBracketMistakes() {
        String[] series = new String[]{
                CPUGraphCreator.buildSeriesDataString(tpsList),
                PlayerActivityGraphCreator.buildSeriesDataString(tpsList),
                PunchCardGraphCreator.createDataSeries(sessionList),
                RamGraphCreator.buildSeriesDataString(tpsList),
                TPSGraphCreator.buildSeriesDataString(tpsList),
                WorldLoadGraphCreator.buildSeriesDataStringChunks(tpsList),
                WorldLoadGraphCreator.buildSeriesDataStringEntities(tpsList),
                WorldMapCreator.createDataSeries(geoList),
                Arrays.toString(WorldPieCreator.createSeriesData(worldTimes))
        };
        for (String test : series) {
            int opened = StringUtils.countMatches(test, "{");
            int closed = StringUtils.countMatches(test, "}");
            Assert.assertEquals(opened, closed);
            opened = StringUtils.countMatches(test, "[");
            closed = StringUtils.countMatches(test, "]");
            Assert.assertEquals(opened, closed);
        }
    }

    @Test
    public void testSeriesCreator() {
        String result = StringUtils.removeAll(SeriesCreator.seriesGraph(points, false, false), "[\\[\\]]");
        String[] splittedResult = result.split(",");

        Map<String, String> expected = new LinkedHashMap<>();

        for (int i = 0; i < splittedResult.length; i++) {
            expected.put(splittedResult[i++], splittedResult[i]);
        }

        int i2 = 0;
        for (Map.Entry<String, String> entry : expected.entrySet()) {
            String expectedX = entry.getKey();
            String expectedY = entry.getValue();

            Point point = points.get(i2);

            assertEquals("Given X does not match expected X", expectedX, String.valueOf((long) point.getX()));
            assertEquals("Given Y does not match expected Y", expectedY, String.valueOf(point.getY()));

            i2++;
        }
    }
}
