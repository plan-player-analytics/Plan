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
package com.djrapitops.plan.query;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.QueryAPIExecutable;
import com.djrapitops.plan.storage.database.queries.QueryAPIQuery;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Consumer;

@Singleton
public class QuerySvc implements QueryService {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ErrorLogger errorLogger;

    private final Set<Consumer<UUID>> playerRemoveSubscribers;
    private final Set<VoidFunction> clearSubscribers;

    @Inject
    public QuerySvc(
            PlanConfig config,
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.errorLogger = errorLogger;

        playerRemoveSubscribers = new HashSet<>();
        clearSubscribers = new HashSet<>();
    }

    public void register() {
        Holder.set(this);
    }

    @Override
    public String getDBType() {
        Database database = dbSystem.getDatabase();
        if (database == null) throw new IllegalStateException("Database has not been initialized.");
        return database.getType().name();
    }

    @Override
    public <T> T query(String sql, ThrowingFunction<PreparedStatement, T> performQuery) {
        return dbSystem.getDatabase().query(new QueryAPIQuery<>(sql, performQuery));
    }

    @Override
    public Future<?> execute(String sql, ThrowingConsumer<PreparedStatement> performStatement) {
        return dbSystem.getDatabase().executeTransaction(
                new Transaction() {
                    @Override
                    protected void performOperations() {
                        execute(new QueryAPIExecutable(sql, performStatement));
                    }
                }
        );
    }

    @Override
    public void subscribeToPlayerRemoveEvent(Consumer<UUID> eventListener) {
        playerRemoveSubscribers.add(eventListener);
    }

    @Override
    public void subscribeDataClearEvent(VoidFunction eventListener) {
        clearSubscribers.add(eventListener);
    }

    public void playerRemoved(UUID playerUUID) {
        playerRemoveSubscribers.forEach(subscriber -> {
            try {
                subscriber.accept(playerUUID);
            } catch (DBOpException e) {
                errorLogger.log(L.WARN, e, ErrorContext.builder()
                        .whatToDo("Report to this Query API user " + subscriber.getClass().getName())
                        .related("Subscriber: " + subscriber.getClass().getName()).build());
            }
        });
    }

    public void dataCleared() {
        clearSubscribers.forEach(function -> {
            try {
                function.apply();
            } catch (DBOpException e) {
                errorLogger.log(L.WARN, e, ErrorContext.builder()
                        .whatToDo("Report to this Query API user " + function.getClass().getName())
                        .related("Subscriber: " + function.getClass().getName()).build());
            }
        });
    }

    @Override
    public Optional<UUID> getServerUUID() {
        return serverInfo.getServerUUIDSafe();
    }

    @Override
    public CommonQueries getCommonQueries() {
        Database database = dbSystem.getDatabase();
        if (database == null) throw new IllegalStateException("Database has not been initialized.");
        return new CommonQueriesImplementation(database, config);
    }
}
