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
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.storage.database.queries.*;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Interface for interacting with a Plan SQL database.
 *
 * @author AuroraLS3
 */
public interface Database {

    /**
     * Initializes the Database.
     * <p>
     * Queries can be performed after this request has completed all required transactions for the database operations.
     *
     * @throws DBInitException if Database fails to initiate.
     */
    void init();

    void close();

    /**
     * Execute an SQL Query statement to get a result.
     * <p>
     * This method should only be called from an asynchronous thread.
     *
     * @param query QueryStatement to execute.
     * @param <T>   Type of the object to be returned.
     * @return Result of the query.
     */
    <T> T query(Query<T> query);

    default <T> Optional<T> queryOptional(String sql, RowExtractor<T> rowExtractor, Object... parameters) {
        return query(new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                QueryParameterSetter.setParameters(statement, parameters);
            }

            @Override
            public Optional<T> processResults(ResultSet set) throws SQLException {
                return set.next() ? Optional.ofNullable(rowExtractor.extract(set)) : Optional.empty();
            }
        });
    }

    default <T> List<T> queryList(String sql, RowExtractor<T> rowExtractor, Object... parameters) {
        return queryCollection(sql, rowExtractor, ArrayList::new, parameters);
    }

    default <T> Set<T> querySet(String sql, RowExtractor<T> rowExtractor, Object... parameters) {
        return queryCollection(sql, rowExtractor, HashSet::new, parameters);
    }

    default <C extends Collection<T>, T> C queryCollection(String sql, RowExtractor<T> rowExtractor, Supplier<C> collectionConstructor, Object... parameters) {
        return query(new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                QueryParameterSetter.setParameters(statement, parameters);
            }

            @Override
            public C processResults(ResultSet set) throws SQLException {
                C collection = collectionConstructor.get();
                while (set.next()) {
                    collection.add(rowExtractor.extract(set));
                }
                return collection;
            }
        });
    }

    default <K, V> Map<K, V> queryMap(String sql, MapRowExtractor<K, V> rowExtractor, Object... parameters) {
        return queryMap(sql, rowExtractor, HashMap::new, parameters);
    }

    default <M extends Map<K, V>, K, V> M queryMap(String sql, MapRowExtractor<K, V> rowExtractor, Supplier<M> mapConstructor, Object... parameters) {
        return query(new QueryStatement<>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                QueryParameterSetter.setParameters(statement, parameters);
            }

            @Override
            public M processResults(ResultSet set) throws SQLException {
                M map = mapConstructor.get();
                while (set.next()) {
                    rowExtractor.extract(set, map);
                }
                return map;
            }
        });
    }

    /**
     * Execute an SQL Transaction.
     *
     * @param transaction Transaction to execute.
     * @return Future that is finished when the transaction has been executed.
     */
    CompletableFuture<?> executeTransaction(Transaction transaction);

    /**
     * Used to get the {@code DBType} of the Database
     *
     * @return the {@code DBType}
     * @see DBType
     */
    DBType getType();

    default Sql getSql() {
        return getType().getSql();
    }

    State getState();

    int getTransactionQueueSize();

    /**
     * Possible State changes:
     * CLOSED to PATCHING (Database init),
     * PATCHING to OPEN (Database initialized),
     * OPEN to CLOSING (Database closing),
     * CLOSING to CLOSED (Database closed),
     * PATCHING to CLOSED (Database closed prematurely)
     */
    enum State {
        CLOSED,
        PATCHING,
        OPEN,
        CLOSING
    }
}
