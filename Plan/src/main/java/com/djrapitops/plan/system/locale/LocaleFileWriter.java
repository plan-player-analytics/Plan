package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.system.locale.lang.Lang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.utilities.comparators.LocaleEntryComparator;
import com.djrapitops.plan.utilities.comparators.StringLengthComparator;
import com.djrapitops.plugin.api.config.Config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility for writing a Locale into a file.
 *
 * @author Rsl1122
 */
public class LocaleFileWriter {

    private final Locale locale;

    public LocaleFileWriter(Locale locale) {
        this.locale = locale;
    }

    public void writeToFile(File file) throws IOException {
        // Find longest identifier
        Optional<String> key = LocaleSystem.getIdentifiers().keySet().stream()
                .min(new StringLengthComparator());
        if (!key.isPresent()) {
            throw new IllegalStateException("LocaleSystem defines no Identifiers.");
        }

        for (Lang lang : LocaleSystem.getIdentifiers().values()) {
            if (!locale.containsKey(lang)) {
                locale.put(lang, new Message(lang.getDefault()));
            }
        }

        int length = key.get().length() + 2;
        List<String> lines = locale.entrySet().stream()
                .sorted(new LocaleEntryComparator())
                .map(entry -> {
                    String value = entry.getValue() != null ? entry.getValue().toString() : entry.getKey().getDefault();
                    return getSpacedIdentifier(entry.getKey().getIdentifier(), length) + "|| " + value;
                })
                .collect(Collectors.toList());
        if (!file.exists()) {
            Files.createFile(file.toPath());
        }

        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);

        Config config = ConfigSystem.getConfig();
        config.set(Settings.WRITE_NEW_LOCALE.getPath(), false);
        config.save();
    }

    private String getSpacedIdentifier(String identifier, int length) {
        StringBuilder b = new StringBuilder(identifier);
        while (b.length() < length) {
            b.append(" ");
        }
        return b.toString();
    }

}