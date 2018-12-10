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
package com.djrapitops.plan.utilities.file;

import com.djrapitops.plan.utilities.java.VoidFunction;

import java.io.File;
import java.nio.file.Path;

/**
 * File with a consumer that is called if the file is modified.
 *
 * @author Rsl1122
 */
public class WatchedFile {

    private final File file;
    private final VoidFunction onChange;

    public WatchedFile(File file, VoidFunction onChange) {
        this.file = file;
        this.onChange = onChange;
    }

    public void modified(Path modifiedPath) {
        if (modifiedPath != null && file.toPath().equals(modifiedPath)) {
            onChange.apply();
        }
    }
}
