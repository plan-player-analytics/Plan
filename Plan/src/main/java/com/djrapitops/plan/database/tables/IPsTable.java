package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.processing.ExecStatement;
import main.java.com.djrapitops.plan.database.processing.QueryAllStatement;
import main.java.com.djrapitops.plan.database.processing.QueryStatement;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

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

    @Override
    public void createTable() throws DBCreateTableException {
        createTable(TableSqlParser.createTable(tableName)
                .column(columnUserID, Sql.INT).notNull()
                .column(columnIP, Sql.varchar(39)).notNull()
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
        String sql = "SELECT DISTINCT " + column + " FROM " + tableName +
                " WHERE " + columnUserID + "=" + usersTable.statementSelectID;


        return query(new QueryStatement<List<String>>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> stringList = new ArrayList<>();
                while (set.next()) {
                    stringList.add(set.getString(column));
                }
                return stringList;
            }
        });
    }

    public void saveIP(UUID uuid, String ip, String geolocation) throws SQLException {
        List<String> ips = getIps(uuid);
        if (ips.contains(ip)) {
            return;
        }
        insertIp(uuid, ip, geolocation);
    }

    private void insertIp(UUID uuid, String ip, String geolocation) throws SQLException {
        execute(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, uuid.toString());
                statement.setString(2, ip);
                statement.setString(3, geolocation);
            }
        });
    }

    public Optional<String> getGeolocation(String ip) throws SQLException {
        String sql = Select.from(tableName, columnGeolocation)
                .where(columnIP + "=?")
                .toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, ip);
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(columnGeolocation));
                }
                return Optional.empty();
            }
        });
    }

    public Map<UUID, List<String>> getAllGeolocations() throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String sql = "SELECT " +
                columnGeolocation + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID;

        return query(new QueryAllStatement<Map<UUID, List<String>>>(sql, 50000) {
            @Override
            public Map<UUID, List<String>> processResults(ResultSet set) throws SQLException {
                Map<UUID, List<String>> geoLocations = new HashMap<>();
                while (set.next()) {
                    UUID uuid = UUID.fromString(set.getString("uuid"));

                    List<String> userGeoLocs = geoLocations.getOrDefault(uuid, new ArrayList<>());
                    userGeoLocs.add(set.getString(columnGeolocation));
                    geoLocations.put(uuid, userGeoLocs);
                }
                return geoLocations;
            }
        });
    }

    public Map<UUID, Map<String, String>> getAllIPsAndGeolocations() throws SQLException {
        String usersIDColumn = usersTable + "." + usersTable.getColumnID();
        String usersUUIDColumn = usersTable + "." + usersTable.getColumnUUID() + " as uuid";
        String sql = "SELECT " +
                columnGeolocation + ", " +
                columnIP + ", " +
                usersUUIDColumn +
                " FROM " + tableName +
                " JOIN " + usersTable + " on " + usersIDColumn + "=" + columnUserID;

        return query(new QueryAllStatement<Map<UUID, Map<String, String>>>(sql, 50000) {
            @Override
            public Map<UUID, Map<String, String>> processResults(ResultSet set) throws SQLException {
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
            }
        });
    }

    public void insertIPsAndGeolocations(Map<UUID, Map<String, String>> allIPsAndGeolocations) throws SQLException {
        if (Verify.isEmpty(allIPsAndGeolocations)) {
            return;
        }

        executeBatch(new ExecStatement(insertStatement) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
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
            }
        });
    }
}
