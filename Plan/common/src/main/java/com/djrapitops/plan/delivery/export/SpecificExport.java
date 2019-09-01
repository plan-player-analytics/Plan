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

import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.file.PlanFiles;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Abstract Html Export Task.
 *
 * // TODO Export should check config settings
 *
 * @author Rsl1122
 */
@Deprecated
public abstract class SpecificExport {

    private final PlanFiles files;
    protected final ServerInfo serverInfo;

    SpecificExport(
            PlanFiles files,
            ServerInfo serverInfo
    ) {
        this.files = files;
        // Hacky, TODO export needs a rework
        this.serverInfo = serverInfo;
    }

    protected File getFolder() {
        File folder;

        String path = getPath();
        boolean isAbsolute = Paths.get(path).isAbsolute();
        if (isAbsolute) {
            folder = new File(path);
        } else {
            File dataFolder = files.getDataFolder();
            folder = new File(dataFolder, path);
        }

        if (!folder.exists() || !folder.isDirectory()) {
            folder.mkdirs();
        }
        return folder;
    }

    protected abstract String getPath();

    protected void export(File to, List<String> lines) throws IOException {
        Files.write(to.toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

}
