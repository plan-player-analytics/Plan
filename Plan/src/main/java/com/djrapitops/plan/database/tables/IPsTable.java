package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class IPsTable extends UserIDTable {

    // TODO Write tests

    private final String columnIP = "ip";
    private final String columnGeolocation = "geolocation";

    /**
     * @param db         The database
     * @param usingMySQL if the server is using MySQL
     */
    public IPsTable(SQLDB db, boolean usingMySQL) {
        super("plan_ips", db, usingMySQL);
    }

    /**
     * @return if the table was created successfully
     */
    @Override
    public boolean createTable() {
        return createTable(TableSqlParser.createTable(tableName)
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
        try {
            List<String> stringList = new ArrayList<>();

            statement = prepareStatement(Select.from(tableName, column)
                    .where(columnUserID + "=" + usersTable.statementSelectID)
                    .toString());
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
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnIP + ", "
                    + columnGeolocation
                    + ") VALUES ("
                    + usersTable.statementSelectID + ", "
                    + "?, ?)");
            statement.setString(1, uuid.toString());
            statement.setString(2, ip);
            statement.setString(3, geolocation);
            statement.execute();
        } finally {
            close(statement);
        }
    }

    public Optional<String> getGeolocation(String ip) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnGeolocation)
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
}
