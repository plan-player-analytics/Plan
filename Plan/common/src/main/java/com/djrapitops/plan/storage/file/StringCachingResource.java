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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Resource decorator to cache result of asString method call in {@link ResourceCache}.
 *
 * @author AuroraLS3
 */
public class StringCachingResource implements Resource {

    private final Resource implementation;

    StringCachingResource(Resource implementation) {
        this.implementation = implementation;
    }

    @Override
    public String getResourceName() {
        return implementation.getResourceName();
    }

    @Override
    public long getLastModifiedDate() {return implementation.getLastModifiedDate();}

    @Override
    public InputStream asInputStream() throws IOException {
        return implementation.asInputStream();
    }

    @Override
    public List<String> asLines() throws IOException {
        return implementation.asLines();
    }

    @Override
    public String asString() throws IOException {
        String got = implementation.asString();
        ResourceCache.cache(implementation.getResourceName(), got, implementation.getLastModifiedDate());
        return got;
    }

    @Override
    public byte[] asBytes() throws IOException {
        return implementation.asBytes();
    }
}