package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.delivery.domain.WebUser;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import utilities.TestConstants;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

class ActiveCookieStoreTest {

    private ActiveCookieStore underTest;
    private User user;

    @BeforeEach
    void createActiveCookieStore() {
        DBSystem dbSystem = Mockito.mock(DBSystem.class);
        Database db = Mockito.mock(Database.class);
        when(dbSystem.getDatabase()).thenReturn(db);

        underTest = new ActiveCookieStore(
                Mockito.mock(PlanConfig.class),
                dbSystem,
                Mockito.mock(RunnableFactory.class),
                Mockito.mock(Processing.class)
        );
        user = new User(TestConstants.PLAYER_ONE_NAME, "console", null, PassEncryptUtil.createHash("testPass"), 0, WebUser.getPermissionsForLevel(0));
    }

    @Test
    void cookiesAreStored() {
        String cookie = underTest.generateNewCookie(user);
        User matchingUser = underTest.checkCookie(cookie).orElseThrow(AssertionError::new);

        assertEquals(user, matchingUser);
    }

    @Test
    void cookiesAreRemoved() {
        String cookie = underTest.generateNewCookie(user);

        underTest.removeCookie(cookie);
        assertFalse(underTest.checkCookie(cookie).isPresent());
    }

    @Test
    void usersCookiesAreRemoved() {
        String cookie = underTest.generateNewCookie(user);
        ActiveCookieStore.removeUserCookie(user.getUsername());

        assertFalse(underTest.checkCookie(cookie).isPresent());
    }

}