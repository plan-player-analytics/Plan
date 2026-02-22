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
package com.djrapitops.plan.storage.database.transactions.events;

import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.webserver.http.AccessLogger;
import com.djrapitops.plan.delivery.webserver.http.InternalRequest;
import com.djrapitops.plan.storage.database.sql.tables.AccessLogTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;

public class StoreRequestTransaction extends ThrowawayTransaction {

    private final Set<AccessLogger.LoggedRequest> loggedRequests;

    public StoreRequestTransaction(Set<AccessLogger.LoggedRequest> loggedRequests) {
        this.loggedRequests = loggedRequests;
    }

    public static String getTruncatedURI(Request request, InternalRequest internalRequest) {
        String uri = request != null ? request.getPath().asString() + request.getQuery().asString()
                : internalRequest.getRequestedURIString();
        if (uri == null) {
            uri = "non-HTTP request, missing URI";
        }
        return StringUtils.truncate(uri, 65000);
    }

    @Override
    protected void performOperations() {
        execute(new ExecBatchStatement(AccessLogTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (AccessLogger.LoggedRequest loggedRequest : loggedRequests) {
                    statement.setLong(1, loggedRequest.getTimestamp());
                    statement.setString(2, StringUtils.truncate(loggedRequest.getAccessAddress(), 45));
                    statement.setString(3, loggedRequest.getMethod());
                    statement.setString(4, loggedRequest.getUrl());
                    statement.setInt(5, loggedRequest.getResponseCode());
                    statement.addBatch();
                }
            }
        });
    }
}
