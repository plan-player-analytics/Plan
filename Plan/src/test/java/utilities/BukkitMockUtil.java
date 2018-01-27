/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plugin.StaticHolder;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.logging.Logger;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/**
 * Mocking Utility for Bukkit version of Plan.
 *
 * @author Rsl1122
 */
public class BukkitMockUtil {

    private Plan planMock;

    private BukkitMockUtil() {
    }

    public static BukkitMockUtil init() {
        return new BukkitMockUtil().mockPlugin();
    }

    public BukkitMockUtil mockPlugin() {
        planMock = Mockito.mock(Plan.class);
        StaticHolder.register(Plan.class, planMock);
        StaticHolder.register(planMock);
        return this;
    }

    public BukkitMockUtil withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    public BukkitMockUtil withLogging() {
        doCallRealMethod().when(planMock).log(Mockito.anyString(), Mockito.anyString());
        when(planMock.getLogger()).thenReturn(Logger.getGlobal());
        return this;
    }

    private File getFile(String fileName) {
        return new File(getClass().getResource(fileName).getPath());
    }

    public BukkitMockUtil withPluginDescription() {
        try {
            File pluginYml = getFile("/plugin.yml");
            PluginDescriptionFile description = new PluginDescriptionFile(new FileInputStream(pluginYml));
            when(planMock.getDescription()).thenReturn(description);
        } catch (FileNotFoundException | InvalidDescriptionException e) {
            System.out.println("Error while setting plugin description");
        }
        return this;
    }

    public BukkitMockUtil withResourceFetchingFromJar() throws Exception {
        withPluginFile("config.yml");
        withPluginFile("web/server.html");
        withPluginFile("web/player.html");
        return this;
    }

    private void withPluginFile(String fileName) throws Exception {
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

    public Plan getPlanMock() {
        return planMock;
    }
}