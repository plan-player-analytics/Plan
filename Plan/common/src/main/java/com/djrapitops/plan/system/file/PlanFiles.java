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
package com.djrapitops.plan.system.file;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Abstracts File methods of Plugin classes so that they can be tested without Mocks.
 *
 * @author Rsl1122
 */
@Singleton
public class PlanFiles implements SubSystem {

    private final PlanPlugin plugin;

    private final File dataFolder;
    private final File configFile;

    @Inject
    public PlanFiles(PlanPlugin plugin) {
        this.dataFolder = plugin.getDataFolder();
        this.plugin = plugin;
        this.configFile = getFileFromPluginFolder("config.yml");
    }

    public File getDataFolder() {
        return dataFolder;
    }

    public File getLogsFolder() {
        File folder = getFileFromPluginFolder("logs");
        folder.mkdirs();
        return folder;
    }

    public File getConfigFile() {
        return configFile;
    }

    public File getLocaleFile() {
        return getFileFromPluginFolder("locale.txt");
    }

    public File getFileFromPluginFolder(String name) {
        return new File(dataFolder, name);
    }

    @Override
    public void enable() throws EnableException {
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
     * Read a file from jar as lines.
     *
     * @param fileName Name of the file.
     * @return lines of the file
     * @throws IOException If the resource can not be read.
     */
    public List<String> readFromResource(String fileName) throws IOException {
        return FileUtil.lines(plugin, fileName);
    }

    public InputStream readStreamFromResource(String fileName) throws IOException {
        return plugin.getResource(fileName);
    }

    /**
     * Read a file from jar as a flat String.
     *
     * @param fileName Name of the file
     * @return Flattened lines with {@code \r\n} line separators.
     * @throws IOException If the resource can not be read.
     */
    public String readFromResourceFlat(String fileName) throws IOException {
        return flatten(readFromResource(fileName));
    }

    /**
     * Read a file from jar or /plugins/Plan/ folder.
     *
     * @param fileName Name of the file
     * @return Flattened lines with {@code \r\n} line separators.
     * @throws IOException If the resource can not be read.
     */
    public String readCustomizableResourceFlat(String fileName) throws IOException {
        return flatten(FileUtil.lines(
                plugin, new File(plugin.getDataFolder(), fileName.replace("/", File.separator)), fileName
        ));
    }

    public InputStream readCustomizableResource(String fileName) {
        return FileUtil.stream(plugin, new File(plugin.getDataFolder(), fileName.replace("/", File.separator)), fileName);
    }

    private String flatten(List<String> lines) {
        StringBuilder flat = new StringBuilder();
        for (String line : lines) {
            flat.append(line).append("\r\n");
        }
        return flat.toString();
    }
}
