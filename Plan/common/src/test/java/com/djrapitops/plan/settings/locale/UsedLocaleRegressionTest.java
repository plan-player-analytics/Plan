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

import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsedLocaleRegressionTest {

    // Ensures that all translation keys used in the React project are defined in a lang file.
    // If test fails, add a new line to a com.djrapitops.plan.settings.locale.lang.HtmlLang with the key.
    @Test
    void allUsedTranslationKeysAreDefined() throws IOException {
        Set<String> usedTranslationKeys = readUsedTranslationKeys();
        Set<String> translationKeys = LocaleSystem.getKeys().keySet();

        assertAll("All in-use translation keys should be defined in a Lang enum.",
                usedTranslationKeys.stream()
                        .map(key -> (Executable) () ->
                                assertTrue(
                                        translationKeys.contains(key),
                                        () -> "'" + key + "' is not defined"
                                )
                        )
                        .toArray(Executable[]::new)
        );
    }

    private Set<String> readUsedTranslationKeys() throws IOException {
        Path reactSourceDir = Paths.get("").toAbsolutePath().getParent().resolve("react/dashboard/src");

        // t('translation.key') or {t("translation.key")
        // or {t("translation.key", {interpolation: "value"})}
        Pattern translatePattern = Pattern.compile("[\\s{]t\\((['\"])(.+?)\\1");

        Set<String> foundLangKeys = new HashSet<>();
        Files.walkFileTree(reactSourceDir, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                String contents = new String(Files.readAllBytes(file));

                Matcher scriptMatcher = translatePattern.matcher(contents);
                while (scriptMatcher.find()) {
                    foundLangKeys.add(scriptMatcher.toMatchResult().group(2));
                }

                return super.visitFile(file, attrs);
            }
        });

        return foundLangKeys;
    }

}
