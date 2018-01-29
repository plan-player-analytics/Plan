package com.djrapitops.pluginbridge.plan.litebans;

import com.djrapitops.plan.system.database.databases.sql.tables.Table;
import litebans.api.Database;
import org.bukkit.Bukkit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class responsible for making queries to LiteBans database.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class LiteBansDatabaseQueries extends Table {
    private final Database database;
    private String litebansTable;

    public LiteBansDatabaseQueries() {
        super("litebans", null);
        database = Database.get();
        String tablePrefix = Bukkit.getPluginManager().getPlugin("LiteBans").getConfig().getString("sql.table_prefix");
        litebansTable = tablePrefix + "bans";
    }

    public List<BanObject> getBans() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = database.prepareStatement("SELECT uuid, reason, banned_by_name, until FROM " + litebansTable);
            set = statement.executeQuery();
            return getBanObjects(set);
        } finally {
            close(set);
            close(statement);
        }
    }

    private List<BanObject> getBanObjects(ResultSet set) throws SQLException {
        List<BanObject> bans = new ArrayList<>();
        while (set.next()) {
            String uuidS = set.getString("uuid");
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidS);
            } catch (IllegalArgumentException e) {
                continue;
            }
            String reason = set.getString("reason");
            String bannedBy = set.getString("banned_by_name");
            long time = set.getLong("until");
            bans.add(new BanObject(uuid, reason, bannedBy, time));
        }
        return bans;
    }

    public List<BanObject> getBans(UUID playerUUID) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = database.prepareStatement("SELECT uuid, reason, banned_by_name, until FROM " + litebansTable + " WHERE uuid=?");
            statement.setString(1, playerUUID.toString());
            set = statement.executeQuery();
            return getBanObjects(set);
        } finally {
            close(set);
            close(statement);
        }
    }

    @Override
    public void createTable() {
        throw new IllegalStateException("Not Supposed to be called.");
    }
}
