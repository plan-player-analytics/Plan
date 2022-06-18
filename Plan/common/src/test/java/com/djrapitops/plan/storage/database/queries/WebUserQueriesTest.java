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
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieExpiryCleanupTask;
import com.djrapitops.plan.delivery.webserver.auth.ActiveCookieStore;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DatabaseTestPreparer;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RegisterWebUserTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveEverythingTransaction;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveWebUserTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utilities.TestConstants;
import utilities.TestPluginLogger;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public interface WebUserQueriesTest extends DatabaseTestPreparer {

    String WEB_USERNAME = TestConstants.PLAYER_ONE_NAME;

    @Test
    default void userIsRegistered() {
        User expected = new User(WEB_USERNAME, "console", null, PassEncryptUtil.createHash("testPass"), 0, WebUser.getPermissionsForLevel(0));
        db().executeTransaction(new RegisterWebUserTransaction(expected));
        forcePersistenceCheck();

        Optional<User> found = db().query(WebUserQueries.fetchUser(WEB_USERNAME));
        assertTrue(found.isPresent());
        assertEquals(expected, found.get());
    }

    @Test
    default void multipleWebUsersAreFetchedAppropriately() {
        userIsRegistered();
        assertEquals(1, db().query(WebUserQueries.fetchAllUsers()).size());
    }

    @Test
    default void webUserIsRemoved() {
        userIsRegistered();
        db().executeTransaction(new RemoveWebUserTransaction(WEB_USERNAME));
        assertFalse(db().query(WebUserQueries.fetchUser(WEB_USERNAME)).isPresent());
    }

    @Test
    default void removeEverythingRemovesWebUser() {
        userIsRegistered();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(WebUserQueries.fetchAllUsers()).isEmpty());
    }

    @Test
    default void activeCookieStoreSavesCookies() {
        userIsRegistered();
        User user = db().query(WebUserQueries.fetchUser(WEB_USERNAME)).orElseThrow(AssertionError::new);

        ActiveCookieStore cookieStore = createActiveCookieStore();

        String cookie = cookieStore.generateNewCookie(user);

        Map<String, User> result = db().query(WebUserQueries.fetchActiveCookies());
        Map<String, User> expected = Collections.singletonMap(cookie, user);
        assertEquals(expected, result);
    }

    @Test
    default void activeCookieStoreDeletesCookies() {
        userIsRegistered();
        User user = db().query(WebUserQueries.fetchUser(WEB_USERNAME)).orElseThrow(AssertionError::new);

        ActiveCookieStore cookieStore = createActiveCookieStore();

        String cookie = cookieStore.generateNewCookie(user);

        cookieStore.removeCookie(cookie);

        assertTrue(db().query(WebUserQueries.fetchActiveCookies()).isEmpty());
    }

    @Test
    default void webUserRemovalDeletesCookies() {
        userIsRegistered();
        User user = db().query(WebUserQueries.fetchUser(WEB_USERNAME)).orElseThrow(AssertionError::new);

        ActiveCookieStore cookieStore = createActiveCookieStore();

        String cookie = cookieStore.generateNewCookie(user);

        db().executeTransaction(new RemoveWebUserTransaction(WEB_USERNAME));

        assertFalse(cookieStore.checkCookie(cookie).isPresent());

        assertTrue(db().query(WebUserQueries.fetchActiveCookies()).isEmpty());
    }

    private ActiveCookieStore createActiveCookieStore() {
        return new ActiveCookieStore(
                Mockito.mock(ActiveCookieExpiryCleanupTask.class),
                Mockito.mock(PlanConfig.class),
                dbSystem(),
                Mockito.mock(Processing.class),
                new TestPluginLogger());
    }

    @Test
    default void removeEverythingRemovesCookies() {
        activeCookieStoreSavesCookies();
        db().executeTransaction(new RemoveEverythingTransaction());
        assertTrue(db().query(WebUserQueries.fetchActiveCookies()).isEmpty());
    }
}