package com.djrapitops.plan.sponge.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.database.databases.sql.MySQLDB;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * MySQLDB implementation for Sponge since default driver is not available.
 *
 * @author Rsl1122
 */
public class SpongeMySQLDB extends MySQLDB {

    @Override
    public void setupDataSource() throws DBInitException {
        Optional<SqlService> sqlServiceProvider = Sponge.getServiceManager().provide(SqlService.class);
        if (!sqlServiceProvider.isPresent()) {
            return;
        }

        String host = Settings.DB_HOST.toString();
        String port = Integer.toString(Settings.DB_PORT.getNumber());
        String database = Settings.DB_DATABASE.toString();
        String launchOptions = Settings.DB_LAUNCH_OPTIONS.toString();
        if (launchOptions.isEmpty() || !launchOptions.startsWith("?") || launchOptions.endsWith("&")) {
            Log.error("Launch Options were faulty, using default (?rewriteBatchedStatements=true&useSSL=false)");
            launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
        }

        String url = host + ":" + port + "/" + database + launchOptions;
        String username = Settings.DB_USER.toString();
        String password = Settings.DB_PASS.toString();
        try {
            this.dataSource = sqlServiceProvider.get().getDataSource(
                    "jdbc:mysql://" + username + ":" + password + "@" + url
            );
        } catch (SQLException e) {
            throw new DBInitException(e);
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return super.getConnection();
        } catch (SQLException e) {
            if (e.getMessage().contains("has been closed")) {
                try {
                    setupDataSource();
                } catch (DBInitException setupException) {
                    throw new IllegalStateException("Failed to set up a new datasource after connection failure.", setupException);
                }
                return super.getConnection();
            } else {
                throw e;
            }
        }
    }
}