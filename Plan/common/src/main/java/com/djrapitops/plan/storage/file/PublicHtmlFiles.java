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

import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.utilities.dev.Untrusted;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Access to public_html folder and its contents.
 *
 * @author AuroraLS3
 */
@Singleton
public class PublicHtmlFiles {

    private final PlanConfig config;

    @Inject
    public PublicHtmlFiles(PlanConfig config) {
        this.config = config;
    }

    public Optional<Resource> findCustomizedResource(@Untrusted String resourceName) {
        Path customizationDirectory = config.getResourceSettings().getCustomizationDirectory();
        return attemptToFind(customizationDirectory, resourceName)
                .map(found -> new FileResource(resourceName, found));
    }

    public Optional<Resource> findPublicHtmlResource(@Untrusted String resourceName) {
        Path publicHtmlDirectory = config.getResourceSettings().getPublicHtmlDirectory();
        return attemptToFind(publicHtmlDirectory, resourceName)
                .map(found -> new FileResource(resourceName, found));
    }

    private Optional<File> attemptToFind(Path from, @Untrusted String resourceName) {
        if (!Files.exists(from)) {
            try {
                Files.createDirectories(from);
            } catch (IOException e) {
                throw new UncheckedIOException("Could not create folder configured in '" + WebserverSettings.PUBLIC_HTML_PATH.getPath() + "'-setting, please create it manually.", e);
            }
        }
        if (from.toFile().exists() && from.toFile().isDirectory()) {
            @Untrusted Path asPath;
            try {
                asPath = from.resolve(resourceName).normalize();
            } catch (InvalidPathException badCharacter) {
                throw new BadRequestException("Requested resource name contained a bad character.");
            }
            // Path may be absolute due to resolving untrusted path
            if (!asPath.startsWith(from)) {
                return Optional.empty();
            }
            // Now it should be trustworthy
            File found = asPath.toFile();
            if (found.exists()) {
                return Optional.of(found);
            }
        }
        return Optional.empty();
    }
}
