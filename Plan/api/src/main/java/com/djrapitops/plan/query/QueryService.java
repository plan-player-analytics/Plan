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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Service for Query API.
 * <p>
 * Requires Capability QUERY_API
 *
 * @author AuroraLS3
 */
public interface QueryService {

    /**
     * Obtain instance of QueryService.
     *
     * @return QueryService implementation.
     * @throws NoClassDefFoundError  If Plan is not installed and this class can not be found or if older Plan version is installed.
     * @throws IllegalStateException If Plan is installed, but not enabled.
     */
    static QueryService getInstance() {
        return Optional.ofNullable(Holder.service.get())
                .orElseThrow(() -> new IllegalStateException("QueryService has not been initialised yet."));
    }

    /**
     * Get what kind of database is in use.
     *
     * @return SQLITE or MYSQL
     * @throws IllegalStateException If database has not been initialized (Plugin failed to enable)
     */
    String getDBType();

    /**
     * Perform a query against Plan database.
     * <p>
     * Blocks thread until query is complete.
     *
     * @param sql          SQL String to execute, can contain parameterized queries ({@code ?}).
     * @param performQuery set your parameters to the PreparedStatement and execute the query, return results.
     * @param <T>          Type of results.
     * @return The object returned by {@code results}.
     * @throws IllegalStateException If something goes wrong with the query. SQLException might be as cause.
     */
    <T> T query(
            String sql,
            ThrowingFunction<PreparedStatement, T> performQuery
    ) throws IllegalStateException;

    /**
     * Execute SQL against Plan database.
     * <p>
     * Does not block thread, SQL is executed in a single transaction to the database.
     * <p>
     * Differs from {@link QueryService#query(String, ThrowingFunction)} in that no results are returned.
     *
     * @param sql              SQL String to execute, can contain parameterized queries ({@code ?}).
     * @param performStatement set your parameters to the PreparedStatement and execute the statement.
     * @return A Future that tells when the transaction has completed. Blocks thread if Future#get is called.
     * @throws IllegalStateException If something goes wrong with the query. SQLException might be as cause.
     */
    Future<?> execute(
            String sql,
            ThrowingConsumer<PreparedStatement> performStatement
    ) throws IllegalStateException;

    /**
     * Used for getting notified about removal of player data.
     * <p>
     * SQL for removing this player's data should be executed when this occurs.
     * <p>
     * Example usage:
     * {@code subscribeToPlayerRemoveEvent(playerUUID -> { do stuff })}
     *
     * @param eventListener Functional interface that is called on the event.
     */
    void subscribeToPlayerRemoveEvent(Consumer<UUID> eventListener);

    /**
     * Used for getting notified about removal of ALL data.
     * <p>
     * SQL for removing all extra tables (and data) should be performed
     * <p>
     * Example usage:
     * {@code subscribeDataClearEvent(() -> { do stuff })}
     *
     * @param eventListener Functional interface that is called on the event.
     */
    void subscribeDataClearEvent(VoidFunction eventListener);

    /**
     * Get the UUID of this server.
     *
     * @return Optional of the server UUID, empty if server did not start properly.
     */
    Optional<UUID> getServerUUID();

    /**
     * Perform some commonly wanted queries.
     *
     * @return {@link CommonQueries} implementation.
     * @throws IllegalStateException If database has not been initialized (Plugin failed to enable)
     */
    CommonQueries getCommonQueries();

    /**
     * See <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">Functional Interfaces</a>
     */
    @FunctionalInterface
    interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }

    /**
     * See <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">Functional Interfaces</a>
     */
    @FunctionalInterface
    interface ThrowingFunction<T, R> {
        R apply(T t) throws SQLException;
    }

    /**
     * See <a href="https://docs.oracle.com/javase/8/docs/api/java/util/function/package-summary.html">Functional Interfaces</a>
     */
    @FunctionalInterface
    interface VoidFunction {
        void apply();
    }

    class Holder {
        static final AtomicReference<QueryService> service = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(QueryService service) {
            Holder.service.set(service);
        }
    }

}
