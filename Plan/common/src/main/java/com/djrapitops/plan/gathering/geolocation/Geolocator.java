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
package com.djrapitops.plan.gathering.geolocation;

import com.djrapitops.plan.exceptions.PreparationException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Interface for different Geolocation service calls.
 *
 * @author AuroraLS3
 */
public interface Geolocator {

    /**
     * Do everything that is needed for the geolocator to function.
     *
     * @throws IOException          If the preparation fails
     * @throws UnknownHostException If preparation requires internet, but internet is not available.
     * @throws PreparationException If preparation fails due to Plan settings
     */
    void prepare() throws IOException;

    Optional<String> getCountry(InetAddress inetAddress);

    default Optional<String> getCountry(String address) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            return getCountry(inetAddress);
        } catch (UnknownHostException e) {
            return Optional.empty();
        }
    }

}
