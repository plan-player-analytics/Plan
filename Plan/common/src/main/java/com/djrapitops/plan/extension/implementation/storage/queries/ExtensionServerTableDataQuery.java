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
package com.djrapitops.plan.extension.implementation.storage.queries;

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.results.*;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.SQLDB;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.sql.tables.extension.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Query Extension data of x most recent players on a server.
 * <p>
 * Returns Map: Player UUID - {@link ExtensionTabData} (container for provider based data)
 *
 * @author AuroraLS3
 */
public class ExtensionServerTableDataQuery implements Query<Map<UUID, ExtensionTabData>> {

    private final ServerUUID serverUUID;
    private final int xMostRecentPlayers;

    public ExtensionServerTableDataQuery(ServerUUID serverUUID, int xMostRecentPlayers) {
        this.serverUUID = serverUUID;
        this.xMostRecentPlayers = xMostRecentPlayers;
    }

    @Override
    public Map<UUID, ExtensionTabData> executeQuery(SQLDB db) {
        return combine(db.query(fetchPlayerData()), db.query(fetchPlayerGroups()));
    }

    private Map<UUID, ExtensionTabData> combine(Map<UUID, ExtensionTabData> one, Map<UUID, ExtensionTabData> two) {
        for (Map.Entry<UUID, ExtensionTabData> entry : two.entrySet()) {
            UUID playerUUID = entry.getKey();
            ExtensionTabData data = entry.getValue();
            ExtensionTabData existingData = one.get(playerUUID);
            if (existingData != null) {
                existingData.combine(data);
            } else {
                existingData = data;
            }
            one.put(playerUUID, existingData);
        }
        return one;
    }

    private Query<Map<UUID, ExtensionTabData>> fetchPlayerData() {
        String selectLimitedNumberOfPlayerUUIDsByLastSeenDate = SELECT +
                SessionsTable.USER_ID + ",MAX(" + SessionsTable.SESSION_END + ") as last_seen" +
                FROM + SessionsTable.TABLE_NAME +
                GROUP_BY + SessionsTable.USER_ID +
                ORDER_BY + "last_seen DESC LIMIT ?";

        String sql = SELECT +
                "v1." + ExtensionPlayerValueTable.USER_UUID + " as uuid," +
                "v1." + ExtensionPlayerValueTable.BOOLEAN_VALUE + " as boolean_value," +
                "v1." + ExtensionPlayerValueTable.DOUBLE_VALUE + " as double_value," +
                "v1." + ExtensionPlayerValueTable.PERCENTAGE_VALUE + " as percentage_value," +
                "v1." + ExtensionPlayerValueTable.LONG_VALUE + " as long_value," +
                "v1." + ExtensionPlayerValueTable.STRING_VALUE + " as string_value," +
                "v1." + ExtensionPlayerValueTable.COMPONENT_VALUE + " as component_value," +
                "null as group_value," +
                "p1." + ExtensionProviderTable.PROVIDER_NAME + " as provider_name," +
                "p1." + ExtensionProviderTable.TEXT + " as text," +
                "p1." + ExtensionProviderTable.FORMAT_TYPE + " as format_type," +
                "p1." + ExtensionProviderTable.IS_PLAYER_NAME + " as is_player_name," +
                "i1." + ExtensionIconTable.ICON_NAME + " as provider_icon_name," +
                "i1." + ExtensionIconTable.FAMILY + " as provider_icon_family" +
                FROM + ExtensionPlayerValueTable.TABLE_NAME + " v1" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.USER_UUID + "=v1." + ExtensionPlayerValueTable.USER_UUID +
                INNER_JOIN + '(' + selectLimitedNumberOfPlayerUUIDsByLastSeenDate + ") as last_seen_q on last_seen_q.user_id=u." + UsersTable.ID +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p1 on p1." + ExtensionProviderTable.ID + "=v1." + ExtensionPlayerValueTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " e1 on e1." + ExtensionPluginTable.ID + "=p1." + ExtensionProviderTable.PLUGIN_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i1 on i1." + ExtensionIconTable.ID + "=p1." + ExtensionProviderTable.ICON_ID +
                WHERE + "e1." + ExtensionPluginTable.SERVER_UUID + "=?" +
                AND + "p1." + ExtensionProviderTable.SHOW_IN_PLAYERS_TABLE + "=?" +
                AND + "p1." + ExtensionProviderTable.IS_PLAYER_NAME + "=?";

        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, xMostRecentPlayers);       // Limit to x most recently seen players
                statement.setString(2, serverUUID.toString());
                statement.setBoolean(3, true);                  // Select only values that should be shown
                statement.setBoolean(4, false);                 // Don't select player_name String values
            }

            @Override
            public Map<UUID, ExtensionTabData> processResults(ResultSet set) throws SQLException {
                return extractDataByPlayer(set);
            }
        };
    }

    private Query<Map<UUID, ExtensionTabData>> fetchPlayerGroups() {
        String selectLimitedNumberOfPlayerUUIDsByLastSeenDate = SELECT +
                SessionsTable.USER_ID + "," +
                "MAX(" + SessionsTable.SESSION_END + ") as last_seen" +
                FROM + SessionsTable.TABLE_NAME +
                GROUP_BY + SessionsTable.USER_ID +
                ORDER_BY + "last_seen DESC LIMIT ?";

        String sql = SELECT +
                "v1." + ExtensionGroupsTable.USER_UUID + " as uuid," +
                "COALESCE(v1." + ExtensionGroupsTable.GROUP_NAME + ",'None') as group_value," +
                "p1." + ExtensionProviderTable.PROVIDER_NAME + " as provider_name," +
                "p1." + ExtensionProviderTable.TEXT + " as text," +
                "i1." + ExtensionIconTable.ICON_NAME + " as provider_icon_name," +
                "i1." + ExtensionIconTable.FAMILY + " as provider_icon_family" +
                FROM + ExtensionGroupsTable.TABLE_NAME + " v1" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.USER_UUID + "=v1." + ExtensionGroupsTable.USER_UUID +
                INNER_JOIN + '(' + selectLimitedNumberOfPlayerUUIDsByLastSeenDate + ") as last_seen_q on last_seen_q.user_id=u." + UsersTable.ID +
                INNER_JOIN + ExtensionProviderTable.TABLE_NAME + " p1 on p1." + ExtensionProviderTable.ID + "=v1." + ExtensionGroupsTable.PROVIDER_ID +
                INNER_JOIN + ExtensionPluginTable.TABLE_NAME + " e1 on e1." + ExtensionPluginTable.ID + "=p1." + ExtensionProviderTable.PLUGIN_ID +
                LEFT_JOIN + ExtensionIconTable.TABLE_NAME + " i1 on i1." + ExtensionIconTable.ID + "=p1." + ExtensionProviderTable.ICON_ID +
                WHERE + "e1." + ExtensionPluginTable.SERVER_UUID + "=?";

        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, xMostRecentPlayers);       // Limit to x most recently seen players
                statement.setString(2, serverUUID.toString());
            }

            @Override
            public Map<UUID, ExtensionTabData> processResults(ResultSet set) throws SQLException {
                return extractDataByPlayer(set);
            }
        };
    }

    private Map<UUID, ExtensionTabData> extractDataByPlayer(ResultSet set) throws SQLException {
        Map<UUID, ExtensionTabData.Builder> dataByPlayer = new HashMap<>();

        while (set.next()) {
            UUID playerUUID = UUID.fromString(set.getString("uuid"));
            ExtensionTabData.Builder data = dataByPlayer.getOrDefault(playerUUID, new ExtensionTabData.Builder(null));

            ExtensionDescription extensionDescription = extractDescription(set);
            extractAndPutDataTo(data, extensionDescription, set);

            dataByPlayer.put(playerUUID, data);
        }
        return dataByPlayer.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().build()));
    }

    private void extractAndPutDataTo(ExtensionTabData.Builder extensionTab, ExtensionDescription description, ResultSet set) throws SQLException {
        String groupValue = set.getString("group_value");
        if (groupValue != null) {
            extensionTab.putGroupData(ExtensionStringData.regularString(description, groupValue));
            return;
        }

        boolean booleanValue = set.getBoolean(ExtensionServerValueTable.BOOLEAN_VALUE);
        if (!set.wasNull()) {
            extensionTab.putBooleanData(new ExtensionBooleanData(description, booleanValue));
            return;
        }

        double doubleValue = set.getDouble(ExtensionPlayerValueTable.DOUBLE_VALUE);
        if (!set.wasNull()) {
            extensionTab.putDoubleData(new ExtensionDoubleData(description, doubleValue));
            return;
        }

        double percentageValue = set.getDouble(ExtensionServerValueTable.PERCENTAGE_VALUE);
        if (!set.wasNull()) {
            extensionTab.putPercentageData(new ExtensionDoubleData(description, percentageValue));
            return;
        }

        long numberValue = set.getLong(ExtensionPlayerValueTable.LONG_VALUE);
        if (!set.wasNull()) {
            FormatType formatType = FormatType.getByName(set.getString(ExtensionProviderTable.FORMAT_TYPE)).orElse(FormatType.NONE);
            extensionTab.putNumberData(new ExtensionNumberData(description, formatType, numberValue));
            return;
        }

        String stringValue = set.getString(ExtensionPlayerValueTable.STRING_VALUE);
        if (stringValue != null) {
            extensionTab.putStringData(ExtensionStringData.regularString(description, stringValue));
        }

        String componentValue = set.getString(ExtensionPlayerValueTable.COMPONENT_VALUE);
        if (componentValue != null) {
            extensionTab.putComponentData(new ExtensionComponentData(description, componentValue));
        }
    }

    private ExtensionDescription extractDescription(ResultSet set) throws SQLException {
        String name = set.getString("provider_name");
        String text = set.getString(ExtensionProviderTable.TEXT);

        String iconName = set.getString("provider_icon_name");
        Family family = Family.getByName(set.getString("provider_icon_family")).orElse(Family.SOLID);
        Icon icon = new Icon(family, iconName, Color.NONE);

        return new ExtensionDescription(name, text, null, icon, 0);
    }
}