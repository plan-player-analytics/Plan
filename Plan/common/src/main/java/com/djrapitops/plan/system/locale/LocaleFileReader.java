/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.lang.Lang;
import com.djrapitops.plan.utilities.file.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utility for reading locale files.
 *
 * @author Rsl1122
 */
public class LocaleFileReader {

    private List<String> lines;

    public LocaleFileReader(File from) throws IOException {
        lines = FileUtil.lines(from);
    }

    public LocaleFileReader(PlanPlugin planPlugin, String fileName) throws IOException {
        lines = FileUtil.lines(planPlugin, "locale/" + fileName);
    }

    public Locale load() {
        Locale locale = new Locale();

        Map<String, Lang> identifiers = LocaleSystem.getIdentifiers();
        lines.forEach(line -> {
            String[] split = line.split(" \\|\\| ");
            if (split.length == 2) {
                String identifier = split[0].trim();
                Lang msg = identifiers.get(identifier);
                if (msg != null) {
                    locale.put(msg, new Message(split[1]));
                }
            }
        });

        return locale;
    }

}