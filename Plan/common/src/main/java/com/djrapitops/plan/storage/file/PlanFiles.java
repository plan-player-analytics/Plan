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
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Inject
    public PlanFiles(
            @Named("dataFolder") File dataFolder,
            JarResource.StreamFunction getResourceStream
    ) {
        this.dataFolder = dataFolder;
        this.getResourceStream = getResourceStream;
        this.configFile = getFileFromPluginFolder("config.yml");
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public Path getDataDirectory() {
        return dataFolder.toPath();
    }

    public Path getCustomizationDirectory() {
        return getDataDirectory().resolve("web");
    }

    public File getLogsFolder() {
        try {
            File folder = getFileFromPluginFolder("logs");
            Files.createDirectories(folder.toPath());
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
        return getFileFromPluginFolder("locale.txt");
    }

    public File getFileFromPluginFolder(String name) {
        return new File(dataFolder, name.replace("/", File.separator));
    }

    @Override
    public void enable() {
        ResourceCache.invalidateAll();
        ResourceCache.cleanUp();
        Verify.isTrue((dataFolder.exists() && dataFolder.isDirectory()) || dataFolder.mkdirs(),
                () -> new EnableException("Could not create data folder at " + dataFolder.getAbsolutePath()));
        try {
            Verify.isTrue((configFile.exists() && configFile.isFile()) || configFile.createNewFile(),
                    () -> new EnableException("Could not create config file at " + configFile.getAbsolutePath()));
        } catch (IOException e) {
            throw new EnableException("Failed to create config.yml", e);
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
    public Resource getResourceFromJar(String resourceName) {
        return new JarResource("assets/plan/" + resourceName, getResourceStream);
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

    public Optional<Resource> getCustomizableResource(String resourceName) {
        return Optional.ofNullable(ResourceCache.getOrCache(resourceName,
                () -> attemptToFind(resourceName)
                        .map(found -> new FileResource(resourceName, found))
                        .orElse(null)
        ));
    }

    private Optional<File> attemptToFind(String resourceName) {
        Path dir = getCustomizationDirectory();
        if (dir.toFile().exists() && dir.toFile().isDirectory()) {
            Path asPath = dir.resolve(resourceName);
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
}
