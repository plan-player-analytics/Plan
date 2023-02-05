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
package com.djrapitops.plan.storage.file;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.CustomizedFileSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import extension.FullSystemExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class PublicHtmlFilesTest {

    @Test
    @DisplayName("findCustomizedResource has no Path Traversal vulnerability")
    void getCustomizableResourceDoesNotAllowAbsolutePathTraversal(@TempDir Path tempDir, PlanConfig config, PublicHtmlFiles files) throws IOException {
        Path directory = tempDir.resolve("customized");
        config.set(CustomizedFileSettings.PATH, directory.toFile().getAbsolutePath());

        Path testFile = tempDir.resolve("file.db");
        Files.createDirectories(directory);
        Files.createDirectories(testFile.getParent());
        Files.createFile(testFile);

        Optional<Resource> resource = files.findCustomizedResource(testFile.toFile().getAbsolutePath());
        assertTrue(resource.isEmpty());
    }

    @Test
    @DisplayName("findPublicHtmlResource has no Path Traversal vulnerability")
    void findPublicHtmlResourceDoesNotAllowAbsolutePathTraversal(@TempDir Path tempDir, PlanConfig config, PublicHtmlFiles files) throws IOException {
        Path directory = tempDir.resolve("public_html");
        config.set(WebserverSettings.PUBLIC_HTML_PATH, directory.toFile().getAbsolutePath());

        Path testFile = tempDir.resolve("file.db");
        Files.createDirectories(directory);
        Files.createDirectories(testFile.getParent());
        Files.createFile(testFile);

        Optional<Resource> resource = files.findPublicHtmlResource(testFile.toFile().getAbsolutePath());
        assertTrue(resource.isEmpty());
    }

    @Test
    @DisplayName("findCustomizedResource finds File")
    void findCustomizedResourceFindsFile(@TempDir Path tempDir, PlanConfig config, PublicHtmlFiles files) throws IOException {
        Path directory = tempDir.resolve("customized");
        config.set(CustomizedFileSettings.PATH, directory.toFile().getAbsolutePath());

        Path testFile = directory.resolve("file.db");
        Files.createDirectories(directory);
        Files.createDirectories(testFile.getParent());
        Files.createFile(testFile);

        Optional<Resource> resource = files.findCustomizedResource("file.db");
        assertTrue(resource.isPresent());
    }

    @Test
    @DisplayName("findPublicHtmlResource finds File")
    void findPublicHtmlResourceFindsFile(@TempDir Path tempDir, PlanConfig config, PublicHtmlFiles files) throws IOException {
        Path directory = tempDir.resolve("public_html");
        config.set(WebserverSettings.PUBLIC_HTML_PATH, directory.toFile().getAbsolutePath());

        Path testFile = directory.resolve("file.db");
        Files.createDirectories(directory);
        Files.createDirectories(testFile.getParent());
        Files.createFile(testFile);

        Optional<Resource> resource = files.findPublicHtmlResource("file.db");
        assertTrue(resource.isPresent());
    }
}