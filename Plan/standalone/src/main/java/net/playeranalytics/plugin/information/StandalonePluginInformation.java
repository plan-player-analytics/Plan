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
package net.playeranalytics.plugin.information;

import net.playeranalytics.plugin.PluginInformation;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * @author AuroraLS3
 */
public class StandalonePluginInformation implements PluginInformation {

    @Override
    public InputStream getResourceFromJar(String resourceName) {
        return getClass().getResourceAsStream("/" + resourceName);
    }

    @Override
    public File getDataFolder() {
        return new File("Plan");
    }

    @Override
    public String getVersion() {
        return readVersionFromPluginYml();
    }

    private String readVersionFromPluginYml() {
        String pluginYmlContents = readAllBytes("plugin.yml");
        for (String line : StringUtils.split(pluginYmlContents, "\n")) {
            if (line.contains("version")) {
                return StringUtils.split(line, ":")[1].trim();
            }
        }
        return "Missing plugin.yml";
    }

    private String readAllBytes(String resource) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream inStream = getResourceFromJar(resource); buffer) {
            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = inStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

}
