package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * The task which handles the upkeep of the {@code Connection}
 *
 * @author Fuzzlemann
 * @since 4.5.1
 */
public class KeepAliveTask extends AbsRunnable {
    private Connection connection;
    private final IReconnect iReconnect;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    public KeepAliveTask(Connection connection, IReconnect iReconnect, PluginLogger logger, ErrorHandler errorHandler) {
        this.connection = connection;
        this.iReconnect = iReconnect;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            if (connection != null && !connection.isClosed()) {
                statement = connection.createStatement();
                resultSet = statement.executeQuery("/* ping */ SELECT 1");
            }
        } catch (SQLException e) {
            logger.debug("Something went wrong during SQL Connection upkeep task.");
            try {
                connection = iReconnect.reconnect();
            } catch (SQLException e1) {
                errorHandler.log(L.ERROR, this.getClass(), e1);
                logger.error("SQL connection maintaining task had to be closed due to exception.");
                this.cancel();
            }
        } finally {
            MiscUtils.close(statement, resultSet);
        }
    }

    public interface IReconnect {
        Connection reconnect() throws SQLException;
    }
}
