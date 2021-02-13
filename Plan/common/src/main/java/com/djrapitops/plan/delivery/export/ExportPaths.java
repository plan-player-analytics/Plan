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

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Export utility that takes care of path replacement for different resources referenced in other files.
 *
 * @author AuroraLS3
 */
public class ExportPaths {

    private final List<String> replace;
    private final List<String> with;

    public ExportPaths() {
        replace = new ArrayList<>();
        with = new ArrayList<>();
    }

    public String resolveExportPaths(String original) {
        return StringUtils.replaceEach(original, replace.toArray(new String[0]), with.toArray(new String[0]));
    }

    public void put(String replace, String with) {
        this.replace.add(replace);
        this.with.add(with);
    }

    public void clear() {
        this.replace.clear();
        this.with.clear();
    }
}