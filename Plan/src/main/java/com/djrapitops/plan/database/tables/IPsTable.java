package main.java.com.djrapitops.plan.database.tables;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public class IPsTable extends Table {

    private final String columnUserID;
    private final String columnIP;

    /**
     *
     * @param db
     * @param usingMySQL
     */
    public IPsTable(SQLDB db, boolean usingMySQL) {
        super("plan_ips", db, usingMySQL);
        columnUserID = "user_id";
        columnIP = "ip";
    }

    /**
     *
     * @return
     */
    @Override
    public boolean createTable() {
        UsersTable usersTable = db.getUsersTable();
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnUserID + " integer NOT NULL, "
                    + columnIP + " varchar(20) NOT NULL, "
                    + "FOREIGN KEY(" + columnUserID + ") REFERENCES " + usersTable.getTableName() + "(" + usersTable.getColumnID() + ")"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    /**
     *
     * @param userId
     * @return
     */
    public boolean removeUserIps(int userId) {
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("DELETE FROM " + tableName + " WHERE (" + columnUserID + "=?)");
            statement.setInt(1, userId);
            statement.execute();
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        } finally {
            close(statement);
        }
    }

    /**
     *
     * @param userId
     * @return
     * @throws SQLException
     */
    public List<InetAddress> getIPAddresses(int userId) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName + " WHERE UPPER(" + columnUserID + ") LIKE UPPER(?)");
            statement.setInt(1, userId);
            set = statement.executeQuery();
            List<InetAddress> ips = new ArrayList<>();
            while (set.next()) {
                try {
                    ips.add(InetAddress.getByName(set.getString(columnIP)));
                } catch (UnknownHostException e) {
                }
            }
            return ips;
        } finally {
            close(set);
            close(statement);
        }
    }

    /**
     *
     * @param userId
     * @param ips
     * @throws SQLException
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
}
