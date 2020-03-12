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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.delivery.domain.WebUser;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RegisterWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveWebUserTransaction;
import org.junit.jupiter.api.Test;
import utilities.TestConstants;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public interface WebUserQueriesTest extends DatabaseTestPreparer {

    @Test
    default void webUserIsRegistered() {
        WebUser expected = new WebUser(TestConstants.PLAYER_ONE_NAME, "RandomGarbageBlah", 0);
        db().executeTransaction(new RegisterWebUserTransaction(expected));
        forcePersistenceCheck();

        Optional<WebUser> found = db().query(WebUserQueries.fetchWebUser(TestConstants.PLAYER_ONE_NAME));
        assertTrue(found.isPresent());
        assertEquals(expected, found.get());
    }

    @Test
    default void multipleWebUsersAreFetchedAppropriately() {
        webUserIsRegistered();
        assertEquals(1, db().query(WebUserQueries.fetchAllPlanWebUsers()).size());
    }

    @Test
    default void webUserIsRemoved() {
        webUserIsRegistered();
        db().executeTransaction(new RemoveWebUserTransaction(TestConstants.PLAYER_ONE_NAME));
        assertFalse(db().query(WebUserQueries.fetchWebUser(TestConstants.PLAYER_ONE_NAME)).isPresent());
    }

}