/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.utilities.html.graphs.line.*;
import com.djrapitops.plan.utilities.html.graphs.stack.AbstractStackGraph;
import org.junit.Before;
import org.junit.Test;
import utilities.RandomData;

import java.util.*;

import static org.junit.Assert.assertTrue;

/**
 * Tests various Graphs.
 *
 * @author Rsl1122
 */
public class GraphTest {

    private final List<TPS> tpsList = new ArrayList<>();
    private final TreeMap<Long, Map<String, Set<UUID>>> activityData = new TreeMap<>();

    @Before
    public void setUp() {
        String[] groups = ActivityIndex.getGroups();
        for (int i = 0; i < 10; i++) {
            tpsList.add(new TPS(i, i, i, i, i, i, i));
            Map<String, Set<UUID>> gData = new HashMap<>();
            for (String group : groups) {
                Set<UUID> uuids = new HashSet<>();
                for (int j = 0; j < RandomData.randomInt(1, 20); j++) {
                    uuids.add(UUID.randomUUID());
                }
                gData.put(group, uuids);
            }
            activityData.put((long) i, gData);
        }
    }

    @Test
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
            System.out.print("Bracket Test: " + graph.getClass().getSimpleName() + " | ");
            String series = graph.toHighChartsSeries();

            System.out.println(series);

            char[] chars = series.toCharArray();
            assertBracketMatch(chars);
        }
    }

    @Test
    public void testStackGraphsForBracketErrors() {
        Settings.FORMAT_DECIMALS.setTemporaryValue("#.##");

        AbstractStackGraph[] graphs = new AbstractStackGraph[]{
                new ActivityStackGraph(activityData)
        };

        for (AbstractStackGraph graph : graphs) {
            System.out.print("Bracket Test: " + graph.getClass().getSimpleName() + " | ");
            String series = graph.toHighChartsSeries();
            System.out.println(series);
            char[] chars = series.toCharArray();
            assertBracketMatch(chars);

            String labels = graph.toHighChartsLabels();
            System.out.println(labels);
            chars = labels.toCharArray();
            assertBracketMatch(chars);
        }
    }

    private void assertBracketMatch(char[] chars) {
        Stack<Character> bracketStack = new Stack<>();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            switch (c) {
                case '{':
                case '[':
                case '(':
                    bracketStack.push(c);
                    break;
                case ')':
                    Character pop = bracketStack.pop();
                    assertTrue("Bracket mismatch at char: " + i + " Expected (, got " + pop, '(' == pop);
                    break;
                case ']':
                    Character pop1 = bracketStack.pop();
                    assertTrue("Bracket mismatch at char: " + i + " Expected [, got " + pop1, '[' == pop1);
                    break;
                case '}':
                    Character pop2 = bracketStack.pop();
                    assertTrue("Bracket mismatch at char: " + i + " Expected {, got " + pop2, '{' == pop2);
                    break;
                default:
                    break;
            }
        }
    }
}
