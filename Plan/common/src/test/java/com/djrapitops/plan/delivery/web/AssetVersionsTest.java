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
package com.djrapitops.plan.delivery.web;

import com.djrapitops.plan.storage.file.PlanFiles;
import extension.FullSystemExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import utilities.TestResources;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author AuroraLS3
 */
@ExtendWith(FullSystemExtension.class)
class AssetVersionsTest {

    @Test
        // This test may fail if new themes are added, but determineAssetModifications task is not run
    void themeNamesAreCorrect(PlanFiles files) throws IOException {
        List<String> themeNames = new AssetVersions(files).getThemeNames();
        List<String> expected = getFileNamesInFolder(TestResources.getAsset("themes"))
                .filter(file -> file.endsWith("json"))
                .map(file -> file.substring(0, file.indexOf('.')))
                .toList();
        assertEquals(expected, themeNames);
    }

    private Stream<String> getFileNamesInFolder(File folder) {
        return Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .filter(Objects::nonNull)
                .filter(File::isFile)
                .map(File::getName);
    }
}