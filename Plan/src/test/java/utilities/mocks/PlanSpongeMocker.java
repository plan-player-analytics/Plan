/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.djrapitops.plugin.task.thread.ThreadRunnableFactory;
import org.mockito.Mockito;
import utilities.mocks.objects.TestLogger;

import java.io.File;

import static org.mockito.Mockito.*;

/**
 * Mocking Utility for Sponge version of Plan.
 *
 * @author Rsl1122
 */
public class PlanSpongeMocker extends Mocker {

    private PlanSponge planMock;

    private PlanSpongeMocker() {
    }

    public static PlanSpongeMocker setUp() {
        return new PlanSpongeMocker().mockPlugin();
    }

    private PlanSpongeMocker mockPlugin() {
        planMock = Mockito.mock(PlanSponge.class);
        super.planMock = planMock;

        doReturn("4.2.0").when(planMock).getVersion();
        doCallRealMethod().when(planMock).getColorScheme();

        ThreadRunnableFactory runnableFactory = new ThreadRunnableFactory();
        doReturn(runnableFactory).when(planMock).getRunnableFactory();

        return this;
    }

    public PlanSpongeMocker withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    public PlanSpongeMocker withLogging() {
        TestLogger testLogger = new TestLogger();
        doReturn(testLogger).when(planMock).getLogger();
        TestPluginLogger testPluginLogger = new TestPluginLogger();
        doReturn(testPluginLogger).when(planMock).getPluginLogger();
        ConsoleErrorLogger consoleErrorLogger = new ConsoleErrorLogger(testPluginLogger);
        doReturn(consoleErrorLogger).when(planMock).getErrorHandler();
        return this;
    }

    public PlanSpongeMocker withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    public PlanSponge getPlanMock() {
        return planMock;
    }
}
