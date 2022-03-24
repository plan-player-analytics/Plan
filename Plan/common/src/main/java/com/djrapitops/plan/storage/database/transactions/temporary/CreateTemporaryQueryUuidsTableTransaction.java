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
package com.djrapitops.plan.storage.database.transactions.temporary;

import com.djrapitops.plan.storage.database.sql.building.CreateTableBuilder;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.transactions.ThrowawayTransaction;
import org.apache.commons.text.TextStringBuilder;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class CreateTemporaryQueryUuidsTableTransaction extends ThrowawayTransaction {

    private final Collection<UUID> uuids;

    private String tableName;

    public CreateTemporaryQueryUuidsTableTransaction(Collection<UUID> uuids) {
        this.uuids = uuids;
    }

    @Override
    protected void performOperations() {
        tableName = "plan_query_uuids_" + System.currentTimeMillis();
        String temporaryTable = CreateTableBuilder.createTemporary(tableName, dbType)
                .column("uuid", Sql.varchar(36))
                .build();
        execute(temporaryTable);

        String insert = "INSERT INTO " + tableName + " (uuid) VALUES ('" + new TextStringBuilder().appendWithSeparators(uuids, "','").build() + "')";
        execute(insert);
    }

    public Optional<String> getTableName() {
        return wasSuccessful() ? Optional.of(tableName) : Optional.empty();
    }
}
