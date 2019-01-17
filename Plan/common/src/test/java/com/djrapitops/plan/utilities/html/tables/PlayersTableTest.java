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
package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.api.ServerAPI;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.utilities.uuid.UUIDUtility;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PlayersTable}
 *
 * @author Rsl1122
 */
public class PlayersTableTest {

    @BeforeClass
    public static void setUpClass() {
        new ServerAPI(
                Mockito.mock(UUIDUtility.class),
                Mockito.mock(HookHandler.class),
                Mockito.mock(DBSystem.class),
                new ConsoleErrorLogger(new TestPluginLogger())
        );
    }

    @Test
    public void noClassCastExceptionsFromFormatting() {
        PlayerContainer container = new PlayerContainer();
        container.putRawData(PlayerKeys.SESSIONS, new ArrayList<>());
        List<PlayerContainer> players = Collections.singletonList(container);
        String html = new PlayersTable(
                players,
                50, // maxPlayers
                TimeUnit.MINUTES.toMillis(60), // activeMsThreshold
                5, // activeLoginThreshold
                false,
                l -> "",
                l -> "",
                d -> ""
        ).parseHtml();

        testHtmlValidity(html);
    }

    private void testHtmlValidity(String html) {
        Stack<String> stack = new Stack<>();
        String[] split = html.split("<");
        for (String s : split) {
            if (s.startsWith("/")) {
                String expectedElement = stack.pop();
                assertTrue("Element not properly closed: " + expectedElement, s.startsWith("/" + expectedElement));
            } else {
                stack.push(s.split(" ", 2)[0].split(">", 2)[0]);
            }
        }
        stack.pop(); // Pop the empty string since the html string starts with <
        assertTrue("Stack was not empty: " + stack.toString(), stack.empty());
    }
}