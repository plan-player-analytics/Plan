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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.Config;
import com.djrapitops.plan.settings.config.ConfigReader;
import com.djrapitops.plan.storage.database.queries.QueryStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;
import static com.djrapitops.plan.storage.database.sql.tables.SettingsTable.*;

/**
 * Query to fetch a newer config from the database.
 *
 * @author AuroraLS3
 */
public class NewerConfigQuery extends QueryStatement<Optional<Config>> {

    private static final String SELECT_STATEMENT = SELECT + CONFIG_CONTENT + FROM + TABLE_NAME +
            WHERE + UPDATED + ">?" +
            AND + SERVER_UUID + "=? LIMIT 1";

    private final ServerUUID serverUUID;
    private final long updatedAfter;

    /**
     * Create a new Query.
     *
     * @param serverUUID   UUID of the server
     * @param updatedAfter Epoch ms.
     */
    public NewerConfigQuery(ServerUUID serverUUID, long updatedAfter) {
        super(SELECT_STATEMENT);
        this.serverUUID = serverUUID;
        this.updatedAfter = updatedAfter;
    }

    @Override
    public void prepare(PreparedStatement statement) throws SQLException {
        statement.setLong(1, updatedAfter);
        statement.setString(2, serverUUID.toString());
    }

    @Override
    public Optional<Config> processResults(ResultSet set) throws SQLException {
        if (set.next()) {
            try (ConfigReader reader = new ConfigReader(new Scanner(set.getString(CONFIG_CONTENT)))) {
                return Optional.of(reader.read());
            }
        } else {
            return Optional.empty();
        }
    }
}