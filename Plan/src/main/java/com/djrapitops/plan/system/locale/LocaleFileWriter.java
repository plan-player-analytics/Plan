package com.djrapitops.plan.system.locale;

import com.djrapitops.plan.system.locale.lang.Lang;
import com.djrapitops.plan.utilities.comparators.LocaleEntryComparator;
import com.djrapitops.plan.utilities.comparators.StringLengthComparator;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility for writing a Locale into a file.
 *
 * @author Rsl1122
 */
public class LocaleFileWriter {

    private Locale locale;

    public LocaleFileWriter(Locale locale) {
        this.locale = locale;
    }

    public void writeToFile(File file) throws IOException {
        // Find longest identifier length for spacing
        int length = LocaleSystem.getIdentifiers().keySet().stream()
                .min(new StringLengthComparator())
                .map(String::length).orElse(0) + 2;

        addMissingLang();

        List<String> lines = createLines(length);

        write(file, lines);
    }

    private void write(File file, List<String> lines) throws IOException {
        if (!file.exists()) {
            Files.createFile(file.toPath());
        }
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
    }

    private List<String> createLines(int length) {
        return locale.entrySet().stream()
                .sorted(new LocaleEntryComparator())
                .map(entry -> {
                    String value = entry.getValue() != null ? entry.getValue().toString() : entry.getKey().getDefault();
                    return getSpacedIdentifier(entry.getKey().getIdentifier(), length) + "|| " + value;
                })
                .collect(Collectors.toList());
    }

    private void addMissingLang() {
        for (Lang lang : LocaleSystem.getIdentifiers().values()) {
            if (!locale.containsKey(lang)) {
                locale.put(lang, new Message(lang.getDefault()));
            }
        }
    }

    private String getSpacedIdentifier(String identifier, int length) {
        StringBuilder b = new StringBuilder(identifier);
        while (b.length() < length) {
            b.append(" ");
        }
        return b.toString();
    }

}