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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertTrue(toFile.exists(), () -> "Failed to copy resource: '" + resourcePath + "', it was not written");
    }

    public static void copyResourceToFile(File toFile, InputStream testResource) {
        try (
                InputStream in = testResource;
                OutputStream out = Files.newOutputStream(toFile.toPath())
        ) {
            copy(in, out);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void writeResourceToFile(File toFile, String resourcePath) {
        try (
                InputStream in = PlanPlugin.class.getResourceAsStream(resourcePath);
                OutputStream out = Files.newOutputStream(toFile.toPath())
        ) {
            if (in == null) {
                throw new FileNotFoundException("Resource with name '" + resourcePath + "' not found");
            }
            copy(in, out);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write resource " + resourcePath + " to file " + toFile.getAbsolutePath() + ", " + e.getMessage(), e);
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
        try {
            Path dir = toFile.toPath().getParent();
            if (!Files.isSymbolicLink(dir)) Files.createDirectories(dir);
            if (!toFile.exists()) {
                Files.createFile(toFile.toPath());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to create file " + toFile.getAbsolutePath() + ", " + e.getMessage(), e);
        }
    }

    public static byte[] getJarResourceAsBytes(String pathFromResourcesDirRoot) throws IOException {
        try (InputStream asStream = TestResources.class.getResourceAsStream(pathFromResourcesDirRoot)) {
            return asStream.readAllBytes();
        }
    }
}
