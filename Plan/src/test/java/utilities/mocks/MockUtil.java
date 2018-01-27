/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package utilities.mocks;

import com.djrapitops.plan.PlanPlugin;

import java.io.File;
import java.io.FileInputStream;

import static org.mockito.Mockito.when;

/**
 * Abstract MockUtil for methods that can be used for both Bungee and Bukkit.
 *
 * @author Rsl1122
 */
public abstract class MockUtil {

    protected PlanPlugin planMock;

    protected File getFile(String fileName) {
        return new File(getClass().getResource(fileName).getPath());
    }

    protected void withPluginFile(String fileName) throws Exception {
        if (planMock.getDataFolder() == null) {
            throw new IllegalStateException("withDataFolder needs to be called before setting files");
        }
        try {
            File file = getFile("/" + fileName);
            when(planMock.getResource(fileName)).thenReturn(new FileInputStream(file));
        } catch (NullPointerException e) {
            System.out.println("File is missing! " + fileName);
        }
    }

    protected void withPluginFiles() throws Exception {
        withPluginFile("bungeeconfig.yml");
        withPluginFile("config.yml");
        withPluginFile("web/server.html");
        withPluginFile("web/player.html");
        withPluginFile("web/network.html");
        withPluginFile("web/error.html");
        withPluginFile("themes/theme.yml");
        withPluginFile("DefaultServerInfoFile.yml");
    }

}