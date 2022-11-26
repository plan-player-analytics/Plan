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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.exceptions.ExportException;
import com.djrapitops.plan.settings.config.PlanConfig;
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(FullSystemExtension.class)
class ReactExporterTest {

    @BeforeAll
    static void enableSystem(PlanSystem system) {
        system.enable();
    }

    @AfterAll
    static void disableSystem(PlanSystem system) {
        system.disable();
    }

    @Test
    @Disabled("React files are not in the build folder during testing on Github for some reason")
    void allReactFilesAreExported(PlanConfig config, Exporter exporter) throws ExportException, IOException {
        Path exportPath = config.getPageExportPath();
        exporter.exportReact();

        Path reactBuildPath = Path.of(new File("").getAbsolutePath())
                .resolve("../react/dashboard/build");

        List<Path> filesToExport = Files.list(reactBuildPath)
                .map(path -> path.relativize(reactBuildPath))
                .collect(Collectors.toList());
        List<Path> filesExported = Files.list(exportPath)
                .map(path -> path.relativize(exportPath))
                .collect(Collectors.toList());
        assertEquals(filesToExport, filesExported);
    }
}