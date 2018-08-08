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