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
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.utilities.dev.Untrusted;
import dagger.Lazy;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
    private final PluginLogger logger;

    @Nullable
    private Path statsDirectory;

    @Inject
    public PlanFiles(
            @Named("dataFolder") File dataFolder,
            JarResource.StreamFunction getResourceStream,
            Lazy<AssetVersions> assetVersions,
            PluginLogger logger
    ) {
        this.dataFolder = dataFolder;
        this.getResourceStream = getResourceStream;
        this.assetVersions = assetVersions;
        this.logger = logger;
        this.configFile = getFileFromPluginFolder("config.yml");
    }

    public static OpenOption[] replaceIfExists() {
        return new OpenOption[]{
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
        };
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
            initializeStatsDirectory(dir);
            cleanOldUnusedFolders();
        } catch (IOException e) {
            throw new EnableException("Failed to create config.yml, " + e.getMessage(), e);
        }
    }

    private void initializeStatsDirectory(Path dir) throws IOException {
        Path serverRoot = dir.toAbsolutePath().getParent().getParent();
        try {
            this.statsDirectory = new StatsDirectoryLookup(serverRoot).lookupStatsDirectory()
                    .orElse(null);
        } catch (Exception e) {
            logger.info("Failed stats folder lookup: " + e.getMessage() + " - minecraft statistics will not be stored.");
        }
    }

    private void cleanOldUnusedFolders() {
        Path serverConfiguration = getDataDirectory().resolve("serverConfiguration");
        if (Files.exists(serverConfiguration)) {
            try (Stream<Path> files = Files.walk(serverConfiguration).sorted(Comparator.reverseOrder())) {
                files.forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
            } catch (IOException | UncheckedIOException e) {
                throw new EnableException("Failed to delete Plan serverConfiguration folder - please delete it manually, " + e.getMessage(), e);
            }
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
        String webResourceName = Strings.CS.remove(resourceName, "web/");
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

    public Optional<Resource> getPlayerStatisticsFile(UUID playerUUID) {
        if (statsDirectory == null) return Optional.empty();
        String statFilename = playerUUID.toString() + ".json";
        Path statsFile = statsDirectory.resolve(statFilename);
        if (Files.exists(statsFile) && Files.isRegularFile(statsFile) && Files.isReadable(statsFile)) {
            return Optional.of(new FileResource(statsFile.toFile().getAbsolutePath(), statsFile.toFile()));
        }
        return Optional.empty();
    }

    public Optional<File> attemptToFind(Path dir, @Untrusted String resourceName) {
        if (Files.exists(dir) && Files.isDirectory(dir)) {
            // Path may be absolute due to resolving untrusted path
            @Untrusted Path asPath;
            try {
                asPath = dir.resolve(resourceName).normalize();
            } catch (InvalidPathException badCharacter) {
                throw new BadRequestException("Requested resource name contained a bad character.");
            }
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
