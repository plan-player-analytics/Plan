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

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.LongSupplier;

/**
 * {@link Resource} implementation for something that is read via InputStream.
 *
 * @author AuroraLS3
 */
public class JarResource implements Resource {

    private final String resourceName;
    private final StreamSupplier streamSupplier;
    private final LongSupplier lastModifiedSupplier;

    public JarResource(String resourceName, StreamSupplier streamSupplier, LongSupplier lastModifiedSupplier) {
        this.resourceName = resourceName;
        this.streamSupplier = streamSupplier;
        this.lastModifiedSupplier = lastModifiedSupplier;
    }

    public JarResource(String resourceName, StreamFunction streamFunction, LongSupplier lastModifiedSupplier) {
        this(resourceName, () -> streamFunction.get(resourceName), lastModifiedSupplier);
    }

    @Override
    public long getLastModifiedDate() {
        return lastModifiedSupplier.getAsLong();
    }

    @Override
    public InputStream asInputStream() throws IOException {
        InputStream stream = streamSupplier.get();
        if (stream == null) {
            throw new FileNotFoundException("a Resource was not found inside the jar (" + resourceName + "), " +
                    "Plan does not support /reload or updates using " +
                    "Plugin Managers, restart the server and see if the error persists.");
        }
        return stream;
    }

    @Override
    public List<String> asLines() throws IOException {
        List<String> lines = new ArrayList<>();
        try (
                InputStream inputStream = asInputStream();
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)
        ) {
            while (scanner.hasNextLine()) {
                lines.add(scanner.nextLine());
            }
        }
        return lines;
    }

    @Override
    public String asString() throws IOException {
        StringBuilder flat = new StringBuilder();
        try (
                InputStream inputStream = asInputStream();
                Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)
        ) {
            while (scanner.hasNextLine()) {
                flat.append(scanner.nextLine()).append("\r\n");
            }
        }
        return flat.toString();
    }

    @Override
    public byte[] asBytes() throws IOException {
        try (
                InputStream in = asInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            IOUtils.copy(in, out);
            return out.toByteArray();
        }
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    public interface StreamSupplier {
        InputStream get() throws IOException;
    }

    public interface StreamFunction {
        InputStream get(String value) throws IOException;
    }
}