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

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.component.ComponentSvc;
import com.djrapitops.plan.delivery.DeliveryUtilities;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.queries.filter.QueryFilters;
import com.djrapitops.plan.storage.database.transactions.Executable;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import utilities.TestConstants;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public interface DatabaseTestPreparer {

    String[] worlds = new String[]{"TestWorld", "TestWorld2"};
    UUID playerUUID = TestConstants.PLAYER_ONE_UUID;
    UUID player2UUID = TestConstants.PLAYER_TWO_UUID;
    UUID player3UUID = TestConstants.PLAYER_THREE_UUID;

    Database db();

    ServerUUID serverUUID();

    /**
     * @deprecated Dependencies may not include the full system in the future.
     */
    @Deprecated
    PlanSystem system();

    default PlanConfig config() {
        return system().getConfigSystem().getConfig();
    }

    default DBSystem dbSystem() {
        return system().getDatabaseSystem();
    }

    default ServerInfo serverInfo() {
        return system().getServerInfo();
    }

    default DeliveryUtilities deliveryUtilities() {
        return system().getDeliveryUtilities();
    }

    default ExtensionSvc extensionService() {return system().getApiServices().getExtensionService();}

    default ComponentSvc componentService() {return system().getApiServices().getComponentService();}

    QueryFilters queryFilters();

    default void execute(Executable executable) {
        try {
            db().executeTransaction(new Transaction() {
                @Override
                protected void performOperations() {
                    execute(executable);
                }
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new AssertionError(e);
        }
    }

    default void executeTransactions(Transaction... transactions) {
        for (Transaction transaction : transactions) {
            try {
                db().executeTransaction(transaction).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new AssertionError(e);
            }
        }
    }

    default void forcePersistenceCheck() {
        db().close();
        db().init();
    }
}
