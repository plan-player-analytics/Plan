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
package com.djrapitops.plan.settings.locale;

import com.djrapitops.plan.storage.file.FileResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link LocaleFileWriter}.
 *
 * @author AuroraLS3
 */
class LocaleFileWriterTest {

    @Test
    void writesAllKeys(@TempDir Path tempDir) throws IOException {
        File file = tempDir.resolve("localeFile.yml").toFile();
        new LocaleFileWriter(new Locale()).writeToFile(file);

        long expected = LocaleSystem.getKeys().size();
        long result = FileResource.lines(file).stream().filter(line -> !line.endsWith(":")).count();
        assertEquals(expected, result);
    }

}