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
                60, // activeMinuteThreshold
                5, // activeLoginThreshold
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