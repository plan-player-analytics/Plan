package main.java.com.djrapitops.plan.utilities.html;

import main.java.com.djrapitops.plan.data.Session;
import org.apache.commons.lang.StringUtils;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import test.java.utils.TestInit;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JavaPlugin.class)
public class HtmlStructureTest {

    private Map<String, List<Session>> sessions;

    @Before
    public void setUp() throws Exception {
        TestInit t = TestInit.init();
        sessions = new HashMap<>();
        sessions.put("World 1", new ArrayList<>());
        sessions.get("World 1").add(new Session(1, 12345L, 23455L, 1, 2));
        sessions.get("World 1").add(new Session(2, 23455L, 23457L, 1, 2));
        sessions.put("World 2", new ArrayList<>());
        sessions.get("World 2").add(new Session(3, 23455L, 23457L, 1, 2));
    }

    @Test
    public void createServerOverviewColumn() throws Exception {
        String serverOverviewColumn = HtmlStructure.createServerOverviewColumn(sessions);
        int opened = StringUtils.countMatches(serverOverviewColumn, "<div");
        int closed = StringUtils.countMatches(serverOverviewColumn, "</div");
        System.out.println(opened + " / " + closed);
        assertEquals(opened, closed);
    }

    @Test
    public void createSessionsTabContent() throws Exception {

        List<Session> allSessions = sessions.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        String sessionsTab = HtmlStructure.createSessionsTabContent(sessions, allSessions);
        int opened = StringUtils.countMatches(sessionsTab, "<div");
        int closed = StringUtils.countMatches(sessionsTab, "</div");
        System.out.println(opened + " / " + closed);
        assertEquals(opened, closed);
    }

}