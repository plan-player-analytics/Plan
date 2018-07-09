/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks;

import com.djrapitops.plan.PlanSponge;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.task.ThreadRunnable;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.Teardown;

import java.io.File;

import static org.mockito.Mockito.*;

/**
 * Mocking Utility for Sponge version of Plan.
 *
 * @author Rsl1122
 */
public class SpongeMockUtil extends MockUtil {

    private PlanSponge planMock;

    private SpongeMockUtil() {
    }

    public static SpongeMockUtil setUp() {
        RunnableFactory.activateTestMode();
        Teardown.resetSettingsTempValues();
        return new SpongeMockUtil().mockPlugin();
    }

    private SpongeMockUtil mockPlugin() {
        planMock = Mockito.mock(PlanSponge.class);
        super.planMock = planMock;
        StaticHolder.register(PlanSponge.class, planMock);
        StaticHolder.register(planMock);

        StaticHolder.saveInstance(MockitoJUnitRunner.class, PlanSponge.class);
        StaticHolder.saveInstance(ThreadRunnable.class, PlanSponge.class);

        doReturn("4.2.0").when(planMock).getVersion();
        doCallRealMethod().when(planMock).getColorScheme();

        return this;
    }

    public SpongeMockUtil withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    public SpongeMockUtil withLogging() {
        doNothing().when(planMock).log(Mockito.anyString(), Mockito.anyString());
//        TestLogger testLogger = new TestLogger();
//        doReturn(testLogger).when(planMock).();
        return this;
    }


    public SpongeMockUtil withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    public PlanSponge getPlanMock() {
        return planMock;
    }
}
