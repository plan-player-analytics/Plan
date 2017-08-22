package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Rsl1122
 */
public class IPsTable extends UserIDTable {

    private final String columnIP = "ip";
    private final String columnGeolocation = "geolocation"; // TODO

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
        UsersTable usersTable = db.getUsersTable();
        try {
            execute(TableSqlParser.createTable(tableName)
                    .column(columnUserID, Sql.INT).notNull()
                    .column(columnIP, Sql.varchar(20)).notNull()
                    .foreignKey(columnUserID, usersTable.getTableName(), usersTable.getColumnID())
                    .toString()
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    /**
     * @param userId The User ID from which the IPs should be removed from
     * @return if the IPs were removed successfully
     */
    public boolean removeUserIPs(int userId) {
        return super.removeDataOf(userId);
    }

    /**
     * @param userId The User ID from which the IPs should be retrieved from
     * @return The retrieved IPs
     * @throws SQLException when an error at retrieval happens
     */
    public List<InetAddress> getIPAddresses(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            List<InetAddress> ips = new ArrayList<>();

            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE UPPER(" + columnUserID + ") LIKE UPPER(?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            while (set.next()) {
                String ipAddressName = set.getString(columnIP);
                try {
                    ips.add(InetAddress.getByName(ipAddressName));
                } catch (UnknownHostException e) {
                    Log.error("Host not found at getIPAddresses: " + ipAddressName); //Shouldn't ever happen
                }
            }

            return ips;
        } finally {
            close(set, statement);
        }
    }

    /**
     * @param userId The User ID for which the IPs should be saved for
     * @param ips    The IPs
     * @throws SQLException when an error at saving happens
     */
    public void saveIPList(int userId, Set<InetAddress> ips) throws SQLException {
        if (ips == null) {
            return;
        }

        ips.removeAll(getIPAddresses(userId));

        if (ips.isEmpty()) {
            return;
        }

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnIP
                    + ") VALUES (?, ?)");
            boolean commitRequired = false;

            for (InetAddress ip : ips) {
                if (ip == null) {
                    continue;
                }

                statement.setInt(1, userId);
                statement.setString(2, ip.getHostAddress());
                statement.addBatch();

                commitRequired = true;
            }

            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }

    /**
     * @param ids The User IDs for which the IPs should be retrieved for
     * @return The User IDs corresponding with their used IPs
     * @throws SQLException when an error at retrieval happens
     */
    public Map<Integer, Set<InetAddress>> getIPList(Collection<Integer> ids) throws SQLException {
        if (Verify.isEmpty(ids)) {
            return new HashMap<>();
        }

        Benchmark.start("Get Ips Multiple");
        PreparedStatement statement = null;
        ResultSet set = null;

        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            Map<Integer, Set<InetAddress>> ips = new HashMap<>();

            for (Integer id : ids) {
                ips.put(id, new HashSet<>());
            }

            while (set.next()) {
                Integer id = set.getInt(columnUserID);

                if (!ids.contains(id)) {
                    continue;
                }

                String ipAddressName = set.getString(columnIP);

                try {
                    ips.get(id).add(InetAddress.getByName(ipAddressName));
                } catch (UnknownHostException e) {
                    Log.error("Host not found at getIPAddresses: " + ipAddressName); //Shouldn't ever happen
                }
            }

            return ips;
        } finally {
            close(set, statement);
            Benchmark.stop("Database", "Get Ips Multiple");
        }
    }

    /**
     * @param ips The User IDs corresponding to their IPs used
     * @throws SQLException when an error at saving happens
     */
    public void saveIPList(Map<Integer, Set<InetAddress>> ips) throws SQLException {
        if (Verify.isEmpty(ips)) {
            return;
        }

        Benchmark.start("Save Ips Multiple");
        Map<Integer, Set<InetAddress>> saved = getIPList(ips.keySet());

        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnUserID + ", "
                    + columnIP
                    + ") VALUES (?, ?)");
            boolean commitRequired = false;

            for (Map.Entry<Integer, Set<InetAddress>> entrySet : ips.entrySet()) {
                Integer id = entrySet.getKey();
                Set<InetAddress> ipAddresses = entrySet.getValue();

                Set<InetAddress> s = saved.get(id);

                if (s != null) {
                    ipAddresses.removeAll(s);
                }

                if (ipAddresses.isEmpty()) {
                    continue;
                }

                for (InetAddress ip : ipAddresses) {
                    if (ip == null) {
                        continue;
                    }

                    statement.setInt(1, id);
                    statement.setString(2, ip.getHostAddress());
                    statement.addBatch();
                    commitRequired = true;
                }
            }

            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
            Benchmark.stop("Database", "Save Ips Multiple");
        }
    }
}
