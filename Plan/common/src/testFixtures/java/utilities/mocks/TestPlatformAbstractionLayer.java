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
package utilities.mocks;

import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.PluginInformation;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;
import org.mockito.Mockito;
import utilities.TestPluginLogger;
import utilities.TestResources;
import utilities.mocks.objects.TestRunnableFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestPlatformAbstractionLayer implements PlatformAbstractionLayer {

    private final Path tempDir;

    public TestPlatformAbstractionLayer(Path tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    public PluginLogger getPluginLogger() {
        return new TestPluginLogger();
    }

    @Override
    public Listeners getListeners() {
        return Mockito.mock(Listeners.class);
    }

    @Override
    public RunnableFactory getRunnableFactory() {
        return new TestRunnableFactory();
    }

    @Override
    public PluginInformation getPluginInformation() {
        return new PluginInformation() {
            @Override
            public InputStream getResourceFromJar(String fileName) {
                return getAsInputStream(fileName);
            }

            private File getFile(String fileName) {
                // Read the resource from jar to a temporary file
                File file = getDataDirectory().resolve("jar").resolve(fileName).toFile();
                TestResources.copyResourceIntoFile(file, "/" + fileName);
                return file;
            }

            private InputStream getAsInputStream(String fileName) {
                try {
                    File file = getFile(fileName);
                    return Files.newInputStream(file.toPath());
                } catch (NullPointerException | IOException e) {
                    System.out.println("File is missing! " + fileName);
                }
                return null;
            }

            @Override
            public File getDataFolder() {
                return tempDir.toFile();
            }

            @Override
            public String getVersion() {
                return "1.0.0";
            }
        };
    }
}
