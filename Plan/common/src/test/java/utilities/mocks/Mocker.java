/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks;

import com.djrapitops.plan.PlanPlugin;

import java.io.*;

import static org.mockito.Mockito.doReturn;

/**
 * Abstract Mocker for methods that can be used for both Bungee and Bukkit.
 *
 * @author Rsl1122
 */
abstract class Mocker {

    PlanPlugin planMock;

    File getFile(String fileName) {
        // Read the resource from jar to a temporary file
        File file = new File(new File(planMock.getDataFolder(), "jar"), fileName);
        try {
            file.getParentFile().mkdirs();
            if (!file.exists() && !file.createNewFile()) {
                throw new FileNotFoundException("Could not create file: " + fileName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (InputStream in = PlanPlugin.class.getResourceAsStream(fileName);
             OutputStream out = new FileOutputStream(file)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return file;
    }

    private void withPluginFile(String fileName) throws Exception {
        if (planMock.getDataFolder() == null) {
            throw new IllegalStateException("withDataFolder needs to be called before setting files");
        }
        try {
            File file = getFile("/" + fileName);
            doReturn(new FileInputStream(file)).when(planMock).getResource(fileName);
        } catch (NullPointerException e) {
            System.out.println("File is missing! " + fileName);
        }
    }

    void withPluginFiles() throws Exception {
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
