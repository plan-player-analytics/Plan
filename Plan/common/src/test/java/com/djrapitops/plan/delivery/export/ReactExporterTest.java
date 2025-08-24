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
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import extension.FullSystemExtension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void allReactFilesAreExported(PlanConfig config, Exporter exporter) throws ExportException, IOException {
        config.set(ExportSettings.SERVER_PAGE, true);
        Path exportPath = config.getPageExportPath();
        exporter.exportReact();

        Path reactBuildPath = Path.of(new File("").getAbsolutePath())
                .resolve("../react/dashboard/build");

        List<Path> filesToExport = Files.list(reactBuildPath)
                .filter(path -> !path.endsWith(".map"))
                .map(path -> path.relativize(reactBuildPath))
                .toList();
        List<Path> filesExported = Files.list(exportPath).map(path -> path.relativize(exportPath)).toList();
        assertAll(filesToExport.stream()
                .map(path -> (Executable) () -> assertTrue(filesExported.contains(path)))
                .toList());
    }

    @Test
    void allThemesAreExported(PlanConfig config, Exporter exporter) throws ExportException, IOException {
        config.set(ExportSettings.SERVER_PAGE, true);
        Path exportPath = config.getPageExportPath();
        exporter.exportReact();

        Path themePath = Path.of(new File("").getAbsolutePath())
                .resolve("src/main/resources/assets/plan/themes");

        List<Path> filesToExport = Files.list(themePath)
                .filter(path -> path.toFile().getAbsolutePath().endsWith(".json"))
                .map(path -> path.relativize(themePath))
                .toList();
        assertFalse(filesToExport.isEmpty());
        List<Path> filesExported = Files.list(exportPath).map(path -> path.relativize(exportPath)).toList();
        assertAll(filesToExport.stream()
                .map(path -> (Executable) () -> assertTrue(filesExported.contains(path)))
                .toList());
    }

    @Test
    void noReactFilesAreExported(PlanConfig config, Exporter exporter) throws ExportException, IOException {
        config.set(ExportSettings.PLAYER_PAGES, false);
        config.set(ExportSettings.SERVER_PAGE, false);
        config.set(ExportSettings.PLAYERS_PAGE, false);
        Path exportPath = config.getPageExportPath();
        Files.deleteIfExists(exportPath);

        exporter.exportReact();

        assertFalse(Files.exists(exportPath), () -> {
            try {
                return "Some files got exported: " + Files.list(exportPath)
                        .map(path -> path.relativize(exportPath))
                        .map(s -> s + "\n")
                        .toList();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}