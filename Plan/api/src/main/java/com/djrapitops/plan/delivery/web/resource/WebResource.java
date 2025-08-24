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
package com.djrapitops.plan.delivery.web.resource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a customizable resource.
 * <p>
 * You can use the create methods for simple resources when using {@link com.djrapitops.plan.delivery.web.ResourceService}.
 * <p>
 * It is assumed that any text based files are encoded in UTF-8.
 *
 * @author AuroraLS3
 */
public interface WebResource {

    /**
     * Create a new WebResource from byte array.
     *
     * @param content Bytes of the resource.
     * @return WebResource.
     */
    static WebResource create(byte[] content) {
        return new ByteResource(content);
    }

    /**
     * Create a new WebResource from an UTF-8 String.
     *
     * @param utf8String String in UTF-8 encoding.
     * @return WebResource.
     */
    static WebResource create(String utf8String) {
        return new ByteResource(utf8String.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a new WebResource from an InputStream.
     *
     * @param in InputStream for the resource, closed after inside the method.
     * @return WebResource.
     * @throws IOException If the stream can not be read.
     */
    static WebResource create(InputStream in) throws IOException {
        return create(in, null);
    }

    /**
     * Creates a new WebResource from an InputStream.
     *
     * @param in           InputStream for the resource, closed after inside the method.
     * @param lastModified Epoch millisecond the resource was last modified
     * @return WebResource.
     * @throws IOException If the stream can not be read.
     */
    static WebResource create(InputStream in, Long lastModified) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            return new ByteResource(out.toByteArray(), lastModified);
        } finally {
            in.close();
        }
    }

    /**
     * Create a lazy WebResource that only reads contents if necessary.
     *
     * @param in           Supplier for InputStream, a lazy method that reads input when necessary.
     * @param lastModified Last modified date for the resource.
     * @return WebResource.
     */
    static WebResource create(Supplier<InputStream> in, Long lastModified) {
        return new LazyWebResource(in, () -> {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 InputStream input = in.get()) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = input.read(bytes)) != -1) {
                    out.write(bytes, 0, read);
                }

                return out.toByteArray();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }, lastModified);
    }

    byte[] asBytes();

    /**
     * Return the resource as a UTF-8 String.
     *
     * @return The resource in UTF-8.
     */
    String asString();

    InputStream asStream();

    default Optional<Long> getLastModified() {
        return Optional.empty();
    }

    final class ByteResource implements WebResource {
        private final byte[] content;
        private final Long lastModified;

        public ByteResource(byte[] content) {
            this(content, null);
        }

        public ByteResource(byte[] content, Long lastModified) {
            this.content = content;
            this.lastModified = lastModified;
        }

        @Override
        public byte[] asBytes() {
            return content;
        }

        @Override
        public String asString() {
            return new String(content, StandardCharsets.UTF_8);
        }

        @Override
        public InputStream asStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public Optional<Long> getLastModified() {
            return Optional.ofNullable(lastModified);
        }
    }

    final class LazyWebResource implements WebResource {
        private final Supplier<InputStream> inputStreamSupplier;
        private final Supplier<byte[]> contentSupplier;
        private final Long lastModified;

        public LazyWebResource(Supplier<InputStream> inputStreamSupplier, Supplier<byte[]> contentSupplier, Long lastModified) {
            this.inputStreamSupplier = inputStreamSupplier;
            this.contentSupplier = contentSupplier;
            this.lastModified = lastModified;
        }

        @Override
        public byte[] asBytes() {
            return contentSupplier.get();
        }

        @Override
        public String asString() {
            return new String(asBytes(), StandardCharsets.UTF_8);
        }

        @Override
        public InputStream asStream() {
            return inputStreamSupplier.get();
        }

        @Override
        public Optional<Long> getLastModified() {
            return Optional.ofNullable(lastModified);
        }
    }
}