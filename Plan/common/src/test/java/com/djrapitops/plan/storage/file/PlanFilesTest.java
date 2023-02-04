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

import extension.FullSystemExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class PlanFilesTest {

    @Test
    @DisplayName("getFileFromPluginFolder has no Path Traversal vulnerability")
    void getFileFromPluginFolderDoesNotAllowAbsolutePathTraversal(@TempDir Path tempDir, PlanFiles files) throws IOException {
        Path testFile = tempDir.resolve("file.db");
        Files.createDirectories(tempDir.getParent());
        Files.createFile(testFile);

        File file = files.getFileFromPluginFolder(testFile.toFile().getAbsolutePath());
        assertNotEquals(testFile.toFile().getAbsolutePath(), file.getAbsolutePath());
    }
}