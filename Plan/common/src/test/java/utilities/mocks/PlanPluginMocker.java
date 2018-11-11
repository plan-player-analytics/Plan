/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.djrapitops.plugin.task.thread.ThreadRunnableFactory;
import org.mockito.Mockito;

import java.io.File;

import static org.mockito.Mockito.doReturn;

/**
 * Mocking Utility for Bukkit version of Plan.
 *
 * @author Rsl1122
 */
public class PlanPluginMocker extends Mocker {

    private PlanPlugin planMock;

    private PlanPluginMocker() {
    }

    public static PlanPluginMocker setUp() {
        return new PlanPluginMocker().mockPlugin();
    }

    private PlanPluginMocker mockPlugin() {
        planMock = Mockito.mock(PlanPlugin.class);
        super.planMock = planMock;

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();
        doReturn("1.0.0").when(planMock).getVersion();

        ThreadRunnableFactory runnableFactory = new ThreadRunnableFactory();
        doReturn(runnableFactory).when(planMock).getRunnableFactory();

        return this;
    }

    public PlanPluginMocker withDataFolder(File tempFolder) {
        doReturn(tempFolder).when(planMock).getDataFolder();
        return this;
    }

    public PlanPluginMocker withLogging() {
        TestPluginLogger testPluginLogger = new TestPluginLogger();
        doReturn(testPluginLogger).when(planMock).getPluginLogger();
        ConsoleErrorLogger consoleErrorLogger = new ConsoleErrorLogger(testPluginLogger);
        doReturn(consoleErrorLogger).when(planMock).getErrorHandler();
        return this;
    }

    public PlanPluginMocker withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    public PlanPlugin getPlanMock() {
        return planMock;
    }
}
