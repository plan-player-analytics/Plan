/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.ui.graphs;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.utilities.analysis.Point;
import com.djrapitops.plan.utilities.html.graphs.line.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.utilities.RandomData;
import test.utilities.TestInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Ignore("Test should use Stack instead")
    public void testLineGraphsForBracketErrors() {
        AbstractLineGraph[] graphs = new AbstractLineGraph[]{
                new CPUGraph(tpsList),
                new OnlineActivityGraph(tpsList),
                new RamGraph(tpsList),
                new TPSGraph(tpsList),
                new EntityGraph(tpsList),
                new ChunkGraph(tpsList)
        };

        for (AbstractLineGraph graph : graphs) {
            String series = graph.toHighChartsSeries();
            // TODO Use Stack instead.
        }
    }
}
