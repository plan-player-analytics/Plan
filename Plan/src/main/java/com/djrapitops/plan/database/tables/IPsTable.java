package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class IPsTable extends UserIDTable {

    private final String columnIP = "ip";
    private final String columnGeolocation = "geolocation";
    private String insertStatement;

    /**
     * @param db         The database
     * @param usingMySQL if the server is using MySQL
     */
    public IPsTable(SQLDB db, boolean usingMySQL) {
        super("plan_ips", db, usingMySQL);
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnUserID + ", "
                + columnIP + ", "
                + columnGeolocation
                + ") VALUES ("
                + usersTable.statementSelectID + ", "
                + "?, ?)";
    }

    /**
     * @return if the table was created successfully
     */
    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnIP, Sql.varchar(20)).notNull()
                .column(columnGeolocation, Sql.varchar(50)).notNull()
                .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                .toString()
        );
    }

    /**
     * @param uuid UUID of the user.
     * @return Users's Login Geolocations.
     * @throws SQLException when an error at retrieval happens
     */
    public List<String> getGeolocations(UUID uuid) throws SQLException {
        return getStringList(uuid, columnGeolocation);
    }

    public List<String> getIps(UUID uuid) throws SQLException {
        return getStringList(uuid, columnIP);
    }

    private List<String> getStringList(UUID uuid, String column) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try (Connection connection = getConnection()) {
            List<String> stringList = new ArrayList<>();

            statement = connection.prepareStatement(Select.from(tableName, column)
                    .where(columnUserID + "=" + usersTable.statementSelectID)
                    .toString());
            statement.setFetchSize(50);
            statement.setString(1, uuid.toString());
            set = statement.executeQuery();
            while (set.next()) {
                stringList.add(set.getString(column));
            }

            return stringList;
        } finally {
            close(set, statement);
        }
    }

    public void saveIP(UUID uuid, String ip, String geolocation) throws SQLException {
        List<String> ips = getIps(uuid);
        if (ips.contains(ip)) {
            return;
        }
        insertIp(uuid, ip, geolocation);
    }

    private void insertIp(UUID uuid, String ip, String geolocation) throws SQLException {
        PreparedStatement statement = null;
        try (Connection connection = getConnection()) {

            statement = connection.prepareStatement(insertStatement);
            statement.setString(1, uuid.toString());
            statement.setString(2, ip);
            statement.setString(3, geolocation);
            statement.execute();

            commit(connection);
        } finally {
            close(statement);
        }
    }

    public Optional<String> getGeolocation(String ip) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try (Connection connection = getConnection()) {
            statement = connection.prepareStatement(Select.from(tableName, columnGeolocation)
                    .where(columnIP + "=?")
                    .toString());
            statement.setString(1, ip);
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(set.getString(columnGeolocation));
            }
            return Optional.empty();
        } finally {
            close(set, statement);
        }
    }

    public Map<UUID, List<String>> getAllGeolocations() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try (Connection connection = getConnection()) {
            Map<UUID, List<String>> geoLocations = new HashMap<>();

            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";

            statement = connection.prepareStatement("SELECT " +
                    columnGeolocation + ", " +
                    usersUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID
            );
            statement.setFetchSize(50000);
            set = statement.executeQuery();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid"));

                List<String> userGeoLocs = geoLocations.getOrDefault(uuid, new ArrayList<>());
                userGeoLocs.add(set.getString(columnGeolocation));
                geoLocations.put(uuid, userGeoLocs);
            }
            return geoLocations;
        } finally {
            close(set, statement);
        }
    }

    public Map<UUID, Map<String, String>> getAllIPsAndGeolocations() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try (Connection connection = getConnection()){
            String usersIDColumn = usersTable + "." + usersTable.getColumnID();
            String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";

            statement = connection.prepareStatement("SELECT " +
                    columnGeolocation + ", " +
                    columnIP + ", " +
                    usersUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID
            );
            statement.setFetchSize(5000);
            set = statement.executeQuery();
            Map<UUID, Map<String, String>> map = new HashMap<>();
            while (set.next()) {
                UUID uuid = UUID.fromString(set.getString("uuid"));

                Map<String, String> userMap = map.getOrDefault(uuid, new HashMap<>());

                String geoLocation = set.getString(columnGeolocation);
                String ip = set.getString(columnIP);

                userMap.put(ip, geoLocation);
                map.put(uuid, userMap);
            }
            return map;
        } finally {
            close(set, statement);
        }
    }

    public void insertIPsAndGeolocations(Map<UUID, Map<String, String>> allIPsAndGeolocations) throws SQLException {
        if (Verify.isEmpty(allIPsAndGeolocations)) {
            return;
        }
        PreparedStatement statement = null;
        try (Connection connection = getConnection()){
            statement = connection.prepareStatement(insertStatement);

            // Every User
            for (UUID uuid : allIPsAndGeolocations.keySet()) {
                // Every IP & Geolocation
                for (Map.Entry<String, String> entry : allIPsAndGeolocations.get(uuid).entrySet()) {
                    String ip = entry.getKey();
                    String geoLocation = entry.getValue();

                    statement.setString(1, uuid.toString());
                    statement.setString(2, ip);
                    statement.setString(3, geoLocation);

                    statement.addBatch();
                }
            }

            statement.executeBatch();
            commit(connection);
        } finally {
            close(statement);
        }
    }
}
