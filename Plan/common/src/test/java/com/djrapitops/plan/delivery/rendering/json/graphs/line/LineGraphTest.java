/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.rendering.json.graphs.line;

import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.gathering.domain.TPS;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link LineGraph}.
 *
 * @author AuroraLS3
 */
class LineGraphTest {

    private static List<TPS> DATA;

    @BeforeAll
    static void setUp() {
        DATA = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            DATA.add(new TPS(i, i, i, i, i, i, i, i));
        }
    }

    @Test
    void testLineGraphsForBracketErrors() {
        TPSMutator mutator = new TPSMutator(DATA);
        LineGraph[] graphs = new LineGraph[]{
                new CPUGraph(mutator, true),
                new PlayersOnlineGraph(mutator, false),
                new RamGraph(mutator, true),
                new TPSGraph(mutator, false),
                new EntityGraph(mutator, true),
                new ChunkGraph(mutator, false),
                new DiskGraph(mutator, false)
        };

        for (LineGraph graph : graphs) {
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
                    assertEquals('(', (char) pop, "Bracket mismatch at char: " + i + " Expected (, got " + pop);
                    break;
                case ']':
                    Character pop1 = bracketStack.pop();
                    assertEquals('[', (char) pop1, "Bracket mismatch at char: " + i + " Expected [, got " + pop1);
                    break;
                case '}':
                    Character pop2 = bracketStack.pop();
                    assertEquals('{', (char) pop2, "Bracket mismatch at char: " + i + " Expected {, got " + pop2);
                    break;
                default:
                    break;
            }
        }
    }
}