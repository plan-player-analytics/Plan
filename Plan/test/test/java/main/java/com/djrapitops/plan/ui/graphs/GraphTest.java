/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package test.java.main.java.com.djrapitops.plan.ui.graphs;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.data.time.WorldTimes;
import main.java.com.djrapitops.plan.utilities.analysis.Point;
import main.java.com.djrapitops.plan.utilities.html.graphs.*;
import org.junit.Before;
import org.junit.Test;
import test.java.utils.RandomData;

import java.util.*;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Fuzzlemann
 */
public class GraphTest {

    private final List<TPS> tpsList = new ArrayList<>();
    private final List<Session> sessionList = new ArrayList<>();
    private final Map<String, Integer> geoList = new HashMap<>();
    private final WorldTimes worldTimes = new WorldTimes("WORLD", "SURVIVAL");

    private List<Point> points = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            tpsList.add(new TPS(i, i, i, i, i, i, i));
            sessionList.add(new Session(i, (long) i, (long) i, i, i));
            geoList.put(String.valueOf(i), i);
        }

        points = RandomData.randomPoints();
    }

    @Test
    public void testGraphCreators() {
        assertEquals("[[0,0.0],[9,9.0]]", CPUGraphCreator.buildSeriesDataString(tpsList));
        assertEquals("[[0,0.0],[9,9.0]]", PlayerActivityGraphCreator.buildSeriesDataString(tpsList));
        // TODO Fix TimeZone Dependency of this test
        // assertEquals("[{x:3600000, y:3, z:14, marker: { radius:14}},]", PunchCardGraphCreator.createDataSeries(sessionList));

        assertEquals("[[0,0.0],[9,9.0]]", RamGraphCreator.buildSeriesDataString(tpsList));
        assertEquals("[[0,0.0],[9,9.0]]", TPSGraphCreator.buildSeriesDataString(tpsList));
        assertEquals("[[0,0.0],[9,9.0]]", WorldLoadGraphCreator.buildSeriesDataStringChunks(tpsList));
        assertEquals("[[0,0.0],[9,9.0]]", WorldLoadGraphCreator.buildSeriesDataStringEntities(tpsList));
        assertEquals("[{'code':'1','value':1},{'code':'2','value':2},{'code':'3','value':3},{'code':'4','value':4},{'code':'5','value':5},{'code':'6','value':6},{'code':'7','value':7},{'code':'8','value':8},{'code':'9','value':9}]", WorldMapCreator.createDataSeries(geoList));
        assertEquals(Arrays.toString(WorldPieCreator.createSeriesData(worldTimes)), "[[{name:'WORLD',y:0,drilldown: 'WORLD'}], {name:'WORLD', id:'WORLD',data: []}]");
    }

    @Test
    public void testSeriesCreator() {
        String result = SeriesCreator.seriesGraph(points, false, false).replaceAll("[\\[\\]]", "");
        String[] splittedResult = result.split(",");

        Map<String, String> expected = new LinkedHashMap<>();

        String key = null;
        for (String resultString : splittedResult) {
            if (key == null) {
                key = resultString;
            } else {
                expected.put(key, resultString);
                key = null;
            }
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
