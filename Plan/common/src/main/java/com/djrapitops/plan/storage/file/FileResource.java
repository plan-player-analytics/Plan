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
package com.djrapitops.plan.storage.file;

import com.djrapitops.plan.utilities.dev.Untrusted;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * {@link Resource} implementation for a {@link File}.
 *
 * @author AuroraLS3
 */
public class FileResource implements Resource {

    @Untrusted
    private final String resourceName;
    private final File file;

    public FileResource(@Untrusted String resourceName, File file) {
        this.resourceName = resourceName;
        this.file = file;
    }

    public static List<String> lines(File file) throws IOException {
        List<String> lines = new ArrayList<>();
        if (file != null && file.exists()) {
            try (Stream<String> linesStream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
                lines = linesStream.collect(Collectors.toList());
            }
        }
        return lines;
    }

    @Override
    @Untrusted
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public long getLastModifiedDate() {
        return file.lastModified();
    }

    @Override
    public InputStream asInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public byte[] asBytes() throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    @Override
    public List<String> asLines() throws IOException {
        return lines(file);
    }

    @Override
    public String asString() throws IOException {
        StringBuilder flat = new StringBuilder();
        try (Scanner scanner = new Scanner(file, StandardCharsets.UTF_8)) {
            while (scanner.hasNextLine()) {
                flat.append(scanner.nextLine()).append("\r\n");
            }
        }
        return flat.toString();
    }
}