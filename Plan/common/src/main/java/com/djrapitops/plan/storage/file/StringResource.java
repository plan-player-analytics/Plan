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

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * {@link Resource} implementation for a {@link String}.
 *
 * @author AuroraLS3
 */
public class StringResource implements Resource {

    private final String resourceName;
    private final String resource;
    private final long lastModified;

    StringResource(String resourceName, String resource, long lastModified) {
        this.resourceName = resourceName;
        this.resource = resource;
        this.lastModified = lastModified;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public long getLastModifiedDate() {
        return lastModified;
    }

    @Override
    public InputStream asInputStream() {
        return new ByteArrayInputStream(resource.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public List<String> asLines() {
        return Arrays.asList(StringUtils.split(resource, "\r\n"));
    }

    @Override
    public String asString() {
        return resource;
    }

    @Override
    public byte[] asBytes() {
        return resource.getBytes(StandardCharsets.UTF_8);
    }
}