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

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.delivery.web.AssetVersions;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.utilities.dev.Untrusted;
import dagger.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Abstracts File methods of Plugin classes so that they can be tested without Mocks.
 *
 * @author AuroraLS3
 */
@Singleton
public class PlanFiles implements SubSystem {

    protected final JarResource.StreamFunction getResourceStream;

    private final File dataFolder;
    private final File configFile;

    private final Lazy<AssetVersions> assetVersions;

    @Inject
    public PlanFiles(
            @Named("dataFolder") File dataFolder,
            JarResource.StreamFunction getResourceStream,
            Lazy<AssetVersions> assetVersions
    ) {
        this.dataFolder = dataFolder;
        this.getResourceStream = getResourceStream;
        this.assetVersions = assetVersions;
        this.configFile = getFileFromPluginFolder("config.yml");
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public Path getDataDirectory() {
        return dataFolder.toPath();
    }

    public File getLogsFolder() {
        try {
            File folder = getFileFromPluginFolder("logs");
            Path dir = folder.toPath();
            Files.createDirectories(dir);
            return folder;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Path getLogsDirectory() {
        return getDataDirectory().resolve("logs");
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getLocaleFile() {
        return getFileFromPluginFolder("locale.yml");
    }

    public File getFileFromPluginFolder(@Untrusted String name) {
        return new File(dataFolder, name.replace("/", File.separator));
    }

    @Override
    public void enable() {
        ResourceCache.invalidateAll();
        ResourceCache.cleanUp();
        try {
            Path dir = getDataDirectory();
            if (!Files.isSymbolicLink(dir)) Files.createDirectories(dir);
            if (!configFile.exists()) Files.createFile(configFile.toPath());
        } catch (IOException e) {
            throw new EnableException("Failed to create config.yml, " + e.getMessage(), e);
        }
    }

    @Override
    public void disable() {
        // No disable actions necessary.
    }

    /**
     * Get a file in the jar as a {@link Resource}.
     *
     * @param resourceName Path to the file inside jar/assets/plan/ folder.
     * @return a {@link Resource} for accessing the resource.
     */
    public Resource getResourceFromJar(@Untrusted String resourceName) {
        return new JarResource(
                "assets/plan/" + resourceName,
                getResourceStream,
                () -> getLastModifiedForJarResource(resourceName)
        );
    }

    @NotNull
    protected Long getLastModifiedForJarResource(@Untrusted String resourceName) {
        String webResourceName = StringUtils.remove(resourceName, "web/");
        return assetVersions.get().getAssetVersion(webResourceName)
                .orElseGet(System::currentTimeMillis);
    }

    /**
     * Get a file from plugin folder as a {@link Resource}.
     *
     * @param resourceName Path to the file inside the plugin folder.
     * @return a {@link Resource} for accessing the resource.
     */
    public Resource getResourceFromPluginFolder(String resourceName) {
        return new FileResource(resourceName, getFileFromPluginFolder(resourceName));
    }

    public Optional<File> attemptToFind(Path dir, @Untrusted String resourceName) {
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            // Path may be absolute due to resolving untrusted path
            @Untrusted Path asPath = dir.resolve(resourceName);
            if (!asPath.startsWith(dir)) {
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

    public Path getJSONStorageDirectory() {
        return getDataDirectory().resolve("cached_json");
    }

    public static OpenOption[] replaceIfExists() {
        return new OpenOption[]{
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
        };
    }

    public Path getThemeDirectory() {
        Path themeDirectory = getDataDirectory().resolve("web_themes");
        if (!Files.exists(themeDirectory)) {
            try {
                Files.createDirectories(themeDirectory);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return themeDirectory;
    }
}
