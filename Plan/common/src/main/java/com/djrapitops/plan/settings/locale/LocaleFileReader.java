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
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.settings.locale.lang.Lang;
import com.djrapitops.plan.storage.file.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Utility for reading locale files.
 *
 * @author AuroraLS3
 */
public class LocaleFileReader {

    private final Resource resource;

    public LocaleFileReader(Resource resource) {
        this.resource = resource;
    }

    public Locale load(LangCode code) throws IOException {
        try (ConfigReader reader = new ConfigReader(resource.asInputStream())) {
            Config config = reader.read();
            Locale locale = new Locale(code);
            Map<String, Lang> keys = LocaleSystem.getKeys();

            config.getConfigPaths().forEach(key -> {
                Lang msg = keys.get(key);
                if (msg != null) {
                    locale.put(msg, new Message(config.getString(key)));
                }
            });

            LocaleModifications.apply(locale);
            return locale;
        }
    }

    /**
     * Used to migrate old files to new format.
     */
    public Locale loadLegacy(LangCode code) throws IOException {
        final List<String> lines = resource.asLines();
        Locale locale = new Locale(code);

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