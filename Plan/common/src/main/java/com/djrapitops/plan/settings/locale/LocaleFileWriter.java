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
package com.djrapitops.plan.settings.locale;

import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigWriter;
import com.djrapitops.plan.settings.locale.lang.Lang;

import java.io.File;
import java.io.IOException;

/**
 * Utility for writing a Locale into a file.
 *
 * @author AuroraLS3
 */
public class LocaleFileWriter {

    private final Locale locale;

    public LocaleFileWriter(Locale locale) {
        this.locale = locale;
    }

    public void writeToFile(File file) throws IOException {
        addMissingLang();

        Config writing = new Config(file);
        locale.forEach((lang, message) -> writing.set(lang.getKey(), message.toString()));

        new ConfigWriter(file.toPath()).write(writing);
    }

    private void addMissingLang() {
        for (Lang lang : LocaleSystem.getKeys().values()) {
            locale.computeIfAbsent(lang, k -> new Message(lang.getDefault()));
        }
    }
}
