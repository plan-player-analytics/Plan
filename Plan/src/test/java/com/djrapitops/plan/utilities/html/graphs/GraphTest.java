/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.utilities.html.graphs.line.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.junit.Assert.assertTrue;

/**
 * Tests various Graphs.
 *
 * @author Rsl1122
 */
public class GraphTest {

    private final List<TPS> tpsList = new ArrayList<>();

    @Before
    public void setUp() {
        for (int i = 0; i < 10; i++) {
            tpsList.add(new TPS(i, i, i, i, i, i, i));
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
