/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package test.java.main.java.com.djrapitops.plan.ui.graphs;

import main.java.com.djrapitops.plan.data.Session;
import main.java.com.djrapitops.plan.data.TPS;
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
    private final Map<String, Long> worldTimes = new HashMap<>();

    private List<Point> points = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            tpsList.add(new TPS(i, i, i, i, i, i, i));
            sessionList.add(new Session(i, (long) i, (long) i, i, i));
            geoList.put(String.valueOf(i), i);
            worldTimes.put(String.valueOf(i), (long) i);
        }

        points = RandomData.randomPoints();
    }

    @Test
    public void testGraphCreators() {
        assertEquals(CPUGraphCreator.buildSeriesDataString(tpsList), "[[0,0.0],[9,9.0]]");
        assertEquals(PlayerActivityGraphCreator.buildSeriesDataString(tpsList), "[[0,0.0],[9,9.0]]");
        assertEquals(PunchCardGraphCreator.createDataSeries(sessionList), "[{x:3600000, y:3, z:14, marker: { radius:14}},]");
        assertEquals(RamGraphCreator.buildSeriesDataString(tpsList), "[[0,0.0],[9,9.0]]");
        assertEquals(TPSGraphCreator.buildSeriesDataString(tpsList), "[[0,0.0],[9,9.0]]");
        assertEquals(WorldLoadGraphCreator.buildSeriesDataStringChunks(tpsList), "[[0,0.0],[9,9.0]]");
        assertEquals(WorldLoadGraphCreator.buildSeriesDataStringEntities(tpsList), "[[0,0.0],[9,9.0]]");
        assertEquals(WorldMapCreator.createDataSeries(geoList), "[{'code':'1','value':1},{'code':'2','value':2},{'code':'3','value':3},{'code':'4','value':4},{'code':'5','value':5},{'code':'6','value':6},{'code':'7','value':7},{'code':'8','value':8},{'code':'9','value':9}]");
        assertEquals(WorldPieCreator.createSeriesData(worldTimes), "[{name:'0',y:0},{name:'1',y:1, sliced: true, selected: true},{name:'2',y:2},{name:'3',y:3},{name:'4',y:4},{name:'5',y:5},{name:'6',y:6},{name:'7',y:7},{name:'8',y:8},{name:'9',y:9}]");
    }

    @Test
    public void testSeriesCreator() {
        String result = SeriesCreator.seriesGraph(points, false, false);
        String[] splittedResult = result.split(",");

        Map<String, String> expected = new LinkedHashMap<>();

        String key = null;
        for (String resultString : splittedResult) {
            resultString = resultString.replaceAll("[\\[\\]]", "");

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
