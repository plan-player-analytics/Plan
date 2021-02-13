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
package com.djrapitops.plan.identification.storage;

import com.djrapitops.plan.identification.Server;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface for operating on server information.
 *
 * @author AuroraLS3
 */
public interface ServerLoader {

    /**
     * Load the server information.
     *
     * @param serverUUID
     * @return Optional of the saved information or empty if it has not been stored.
     * @throws com.djrapitops.plan.exceptions.EnableException When the loading fails
     */
    Optional<Server> load(UUID serverUUID);

    /**
     * Save the server information.
     *
     * @param information Information to save.
     * @throws com.djrapitops.plan.exceptions.EnableException When the saving fails
     */
    void save(Server information);

}
