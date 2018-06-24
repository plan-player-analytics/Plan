package com.djrapitops.plan.utilities.html.tables;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import utilities.Teardown;
import utilities.mocks.SystemMockUtil;

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

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws Exception {
        SystemMockUtil.setUp(temporaryFolder.getRoot())
                .enableConfigSystem();
        Teardown.resetSettingsTempValues();
    }

    @Test
    public void noClassCastExceptionsFromFormatting() {
        PlayerContainer container = new PlayerContainer();
        container.putRawData(PlayerKeys.SESSIONS, new ArrayList<>());
        List<PlayerContainer> players = Collections.singletonList(container);
        String html = PlayersTable.forServerPage(players).parseHtml();

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