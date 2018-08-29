/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.utilities.java.ThrowingVoidFunction;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Class that decides what to do with WebExceptions.
 *
 * @author Rsl1122
 */
@Singleton
public class WebExceptionLogger {

    private final ConnectionLog connectionLog;
    private final PluginLogger logger;
    private final ErrorHandler errorHandler;

    @Inject
    public WebExceptionLogger(
            ConnectionLog connectionLog,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.connectionLog = connectionLog;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    public void logIfOccurs(Class definingClass, ThrowingVoidFunction<WebException> function) {
        try {
            function.apply();
        } catch (ConnectionFailException e) {
            if (shouldLog(e)) {
                logger.debug(e.getMessage());
            }
        } catch (UnsupportedTransferDatabaseException | UnauthorizedServerException
                | NotFoundException | NoServersException e) {
            logger.debug(e.getMessage());
        } catch (WebException e) {
            errorHandler.log(L.WARN, definingClass, e);
        }
    }

    private boolean shouldLog(ConnectionFailException e) {
        String address = getAddress(e);
        if (address == null) {
            return true;
        }
        Map<String, Map<String, ConnectionLog.Entry>> logEntries = connectionLog.getLogEntries();
        Map<String, ConnectionLog.Entry> entries = logEntries.get("Out: " + address);
        if (entries != null) {
            List<ConnectionLog.Entry> connections = new ArrayList<>(entries.values());
            Collections.sort(connections);
            return connections.isEmpty() || connections.get(0).getResponseCode() != -1;
        }
        return true;
    }

    private static String getAddress(ConnectionFailException e) {
        if (e.getMessage().contains("to address")) {
            String[] split = e.getMessage().split("to address: ");
            if (split.length == 2) {
                String[] split2 = split[1].split("<br>");
                if (split2.length == 2) {
                    return split2[0];
                }
            }
        }
        return null;
    }
}
