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
package utilities;

import com.djrapitops.plan.PlanPlugin;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertTrue;

public class TestResources {

    private TestResources() {
        /* static method class */
    }

    public static File getTestResourceFile(String called, Class testClass) throws URISyntaxException {
        URL resource = testClass.getResource("/" + called);
        URI resourceURI = resource.toURI();
        Path resourcePath = Paths.get(resourceURI);
        return resourcePath.toFile();
    }

    public static void copyResourceIntoFile(File toFile, String resourcePath) {
        createEmptyFile(toFile);
        writeResourceToFile(toFile, resourcePath);
        assertTrue("Failed to copy resource: '" + resourcePath + "'", toFile.exists());
    }

    public static void copyTestResourceIntoFile(File toFile, InputStream testResource) {
        createEmptyFile(toFile);
        copyResourceToFile(toFile, testResource);
        assertTrue("Failed to copy resource: '" + toFile.getAbsolutePath() + "'", toFile.exists());
    }

    private static void copyResourceToFile(File toFile, InputStream testResource) {
        try (InputStream in = testResource;
             OutputStream out = new FileOutputStream(toFile)) {
            copy(in, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeResourceToFile(File toFile, String resourcePath) {
        try (InputStream in = PlanPlugin.class.getResourceAsStream(resourcePath);
             OutputStream out = new FileOutputStream(toFile)) {
            if (in == null) {
                throw new FileNotFoundException("Resource with name '" + resourcePath + "' not found");
            }
            copy(in, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException {
        int read;
        byte[] bytes = new byte[1024];

        while ((read = in.read(bytes)) != -1) {
            out.write(bytes, 0, read);
        }
    }

    private static void createEmptyFile(File toFile) {
        String path = toFile.getAbsolutePath();
        try {
            toFile.getParentFile().mkdirs();
            if (!toFile.exists() && !toFile.createNewFile()) {
                throw new FileNotFoundException("Could not create file: " + path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
