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
package com.djrapitops.plan.delivery.export;

import com.djrapitops.plan.delivery.webserver.response.ResponseFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Handles exporting of player json.
 *
 * @author Rsl1122
 */
@Singleton
public class PlayerJSONExporter extends FileExporter {

    private final ResponseFactory responseFactory;

    @Inject
    public PlayerJSONExporter(
            ResponseFactory responseFactory
    ) {
        this.responseFactory = responseFactory;
    }

    public void export(Path toDirectory, UUID playerUUID, String playerName) throws IOException {
        Path to = toDirectory.resolve("player/" + toFileName(playerName) + ".json");
        exportJSON(to, playerUUID);
    }

    private void exportJSON(Path to, UUID playerUUID) throws IOException {
        export(to, responseFactory.rawPlayerPageResponse(playerUUID).getContent());
    }
}