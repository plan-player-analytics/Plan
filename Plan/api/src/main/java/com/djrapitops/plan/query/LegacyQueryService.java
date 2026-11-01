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
import java.util.concurrent.Future;

/**
 * Keeps the old {@code Future<?> execute(String, ThrowingConsumer)} signature in the bytecode.
 * <p>
 * Extensions compiled before {@link QueryService#execute(String, QueryService.ThrowingConsumer)}
 * returned {@code CompletableFuture<Void>} still call the old descriptor and would otherwise fail
 * with {@link NoSuchMethodError}. The covariant override in QueryService makes javac emit the bridge.
 *
 * @deprecated Use {@link QueryService} directly.
 */
@Deprecated
public interface LegacyQueryService {

    Future<?> execute(
            String sql,
            QueryService.ThrowingConsumer<PreparedStatement> performStatement
    ) throws IllegalStateException;
}
