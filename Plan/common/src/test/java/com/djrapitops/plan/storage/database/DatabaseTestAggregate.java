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

import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionQueryResultTableDataQueryTest;
import com.djrapitops.plan.storage.database.queries.*;
import com.djrapitops.plan.storage.database.queries.analysis.PlayerRetentionQueriesTest;
import com.djrapitops.plan.storage.database.queries.analysis.TopListQueriesTest;
import com.djrapitops.plan.storage.database.queries.objects.PluginMetadataQueriesTest;
import com.djrapitops.plan.storage.database.transactions.commands.ChangeUserUUIDTransactionTest;
import com.djrapitops.plan.storage.database.transactions.commands.CombineUserTransactionTest;
import com.djrapitops.plan.storage.database.transactions.patches.AfterBadJoinAddressDataCorrectionPatchTest;
import com.djrapitops.plan.storage.database.transactions.patches.BadJoinAddressDataCorrectionPatchTest;

public interface DatabaseTestAggregate extends
        ActivityIndexQueriesTest,
        AllowlistQueriesTest,
        DatabaseBackupTest,
        ExtensionsDatabaseTest,
        GeolocationQueriesTest,
        NicknameQueriesTest,
        PingQueriesTest,
        ServerQueriesTest,
        SessionQueriesTest,
        TopListQueriesTest,
        TPSQueriesTest,
        UserInfoQueriesTest,
        WebUserQueriesTest,
        FilterQueryTest,
        JoinAddressQueriesTest,
        ChangeUserUUIDTransactionTest,
        CombineUserTransactionTest,
        ExtensionQueryResultTableDataQueryTest,
        BadJoinAddressDataCorrectionPatchTest,
        AfterBadJoinAddressDataCorrectionPatchTest,
        PlayerRetentionQueriesTest,
        PluginMetadataQueriesTest {
    /* Collects all query tests together so its easier to implement database tests */
}
