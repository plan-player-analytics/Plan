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
import com.djrapitops.plan.delivery.webserver.http.InternalRequest;
import com.djrapitops.plan.storage.database.sql.tables.AccessLogTable;
import com.djrapitops.plan.storage.database.transactions.ExecStatement;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StoreRequestTransaction extends ThrowawayTransaction {

    private final long timestamp;
    private final String accessAddress;
    private final String method;
    private final String url;
    private final int responseCode;

    public StoreRequestTransaction(long timestamp, String accessAddress, String method, String url, int responseCode) {
        this.timestamp = timestamp;
        this.accessAddress = accessAddress;
        this.method = method;
        this.url = url;
        this.responseCode = responseCode;
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
        execute(new ExecStatement(AccessLogTable.INSERT_NO_USER) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, timestamp);
                statement.setString(2, StringUtils.truncate(accessAddress, 45));
                statement.setString(3, method);
                statement.setString(4, url);
                statement.setInt(5, responseCode);
            }
        });
    }
}
