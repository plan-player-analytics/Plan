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
package com.djrapitops.plan.delivery.webserver.resolver;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author AuroraLS3
 */
public class ETag {

    private final String etag;

    public ETag(String etag) {
        this.etag = etag;
    }

    public String getEtag() {
        return etag;
    }

    public Optional<Long> parseAsLong() {
        try {
            return Optional.of(Long.parseLong(etag));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public boolean isOutdated(long timestamp) {
        return isOutdated(timestamp, () -> "");
    }

    public boolean isOutdated(long timestamp, Supplier<String> hashSupplier) {
        Optional<Long> asLong = parseAsLong();
        if (asLong.isEmpty()) {
            return !etag.equals(hashSupplier.get());
        }
        return asLong.map(t -> t != timestamp).orElse(true);
    }
}
