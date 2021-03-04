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

import org.spongepowered.api.asset.Asset;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * {@link Resource} implementation for Sponge Asset API.
 *
 * @author AuroraLS3
 */
public class SpongeAssetResource implements Resource {

    private final String resourceName;
    private final Asset asset;

    public SpongeAssetResource(String resourceName, Asset asset) {
        this.resourceName = resourceName;
        this.asset = asset;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    private void nullCheck() throws FileNotFoundException {
        if (asset == null) {
            throw new FileNotFoundException("a Resource was not found inside the jar (/assets/plan/" + resourceName + "), " +
                    "Plan does not support /reload or updates using " +
                    "Plugin Managers, restart the server and see if the error persists.");
        }
    }

    @Override
    public InputStream asInputStream() throws IOException {
        nullCheck();
        return new ByteArrayInputStream(asset.readBytes());
    }

    @Override
    public List<String> asLines() throws IOException {
        return asset.readLines(StandardCharsets.UTF_8);
    }

    @Override
    public String asString() throws IOException {
        return asset.readString(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] asBytes() throws IOException {
        return asset.readBytes();
    }
}