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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Represents a customizable resource.
 * <p>
 * You can use the create methods for simple resources when using {@link com.djrapitops.plan.delivery.web.ResourceService}.
 */
public interface Resource {

    static Resource create(byte[] content) {
        return new ByteResource(content);
    }

    static Resource create(String utf8String) {
        return new ByteResource(utf8String.getBytes(StandardCharsets.UTF_8));
    }

    static Resource create(InputStream in) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            return new ByteResource(out.toByteArray());
        } finally {
            in.close();
        }
    }

    byte[] asBytes();

    String asString();

    InputStream asStream();

    final class ByteResource implements Resource {
        private final byte[] content;

        public ByteResource(byte[] content) {
            this.content = content;
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
    }
}