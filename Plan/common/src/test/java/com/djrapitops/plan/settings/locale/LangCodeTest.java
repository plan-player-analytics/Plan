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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class LangCodeTest {

    @Test
    @DisplayName("All LangCodes have matching file")
    void allLangCodesHaveFile() {
        assertAll(Arrays.stream(LangCode.values())
                .filter(Predicate.not(LangCode.CUSTOM::equals))
                .map(langCode -> () -> assertFileExists(langCode))
        );
    }

    private void assertFileExists(LangCode langCode) throws IOException {
        URL resource = getClass().getClassLoader().getResource("assets/plan/locale/" + langCode.getFileName());
        assertNotNull(resource, () -> "Resource assets/plan/locale/" + langCode.getFileName() + " does not exist, but it is needed for LangCode." + langCode.name());

        try (var res = getClass().getClassLoader().getResourceAsStream("assets/plan/locale/" + langCode.getFileName())) {
            assertNotNull(res, () -> "Resource stream assets/plan/locale/" + langCode.getFileName() + " does not exist, but it is needed for LangCode." + langCode.name());
        }
    }

    @Test
    @DisplayName("All locale files have matching LangCode")
    void allFilesHaveLangCode() throws URISyntaxException, IOException {
        Set<String> fileNames = Arrays.stream(LangCode.values())
                .filter(Predicate.not(LangCode.CUSTOM::equals))
                .map(LangCode::getFileName)
                .collect(Collectors.toSet());

        URL resource = getClass().getClassLoader().getResource("assets/plan/locale");
        assertNotNull(resource, "assets/plan/locale folder has gone missing for some reason - It's needed to access locales");

        try (var fileSystem = FileSystems.newFileSystem(resource.toURI(), Map.of())) {
            Path path = fileSystem.getPath("assets/plan/locale");
            try (var paths = Files.walk(path)) {
                List<String> localeFiles = paths
                        .filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .toList();

                assertAll(localeFiles.stream()
                        .map(fileName -> () ->
                                assertTrue(fileNames.contains(fileName), () -> "'" + fileName + "' was not found from assets/plan/locale/")
                        ));
            }
        }
    }

}
