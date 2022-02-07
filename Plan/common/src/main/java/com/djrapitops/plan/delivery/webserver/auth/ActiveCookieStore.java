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
package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.events.CookieChangeTransaction;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class ActiveCookieStore implements SubSystem {

    private static final Map<String, User> USERS_BY_COOKIE = new ConcurrentHashMap<>();
    public static long cookieExpiresAfter = TimeUnit.HOURS.toMillis(2L);
    private static ActiveCookieStore activeCookieStore;

    private final ActiveCookieExpiryCleanupTask activeCookieExpiryCleanupTask;

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Processing processing;

    @Inject
    public ActiveCookieStore(
            ActiveCookieExpiryCleanupTask activeCookieExpiryCleanupTask,
            PlanConfig config,
            DBSystem dbSystem,
            Processing processing
    ) {
        this.activeCookieExpiryCleanupTask = activeCookieExpiryCleanupTask;
        ActiveCookieStore.activeCookieStore = this;

        this.config = config;
        this.dbSystem = dbSystem;
        this.processing = processing;
    }

    private static void removeCookieStatic(String cookie) {
        activeCookieStore.removeCookie(cookie);
    }

    public static void removeUserCookie(String username) {
        USERS_BY_COOKIE.entrySet().stream().filter(entry -> entry.getValue().getUsername().equals(username))
                .findAny()
                .map(Map.Entry::getKey)
                .ifPresent(ActiveCookieStore::removeCookieStatic);
    }

    @Override
    public void enable() {
        cookieExpiresAfter = config.get(WebserverSettings.COOKIES_EXPIRE_AFTER);
        processing.submitNonCritical(this::loadActiveCookies);
    }

    private void loadActiveCookies() {
        USERS_BY_COOKIE.clear();
        USERS_BY_COOKIE.putAll(dbSystem.getDatabase().query(WebUserQueries.fetchActiveCookies()));
        for (Map.Entry<String, Long> entry : dbSystem.getDatabase().query(WebUserQueries.getCookieExpiryTimes()).entrySet()) {
            long timeToExpiry = Math.max(entry.getValue() - System.currentTimeMillis(), 0L);
            activeCookieExpiryCleanupTask.addExpiry(entry.getKey(), System.currentTimeMillis() + timeToExpiry);
        }
    }

    @Override
    public void disable() {
        USERS_BY_COOKIE.clear();
    }

    public Optional<User> checkCookie(String cookie) {
        return Optional.ofNullable(USERS_BY_COOKIE.get(cookie));
    }

    public String generateNewCookie(User user) {
        String cookie = DigestUtils.sha256Hex(user.getUsername() + UUID.randomUUID() + System.currentTimeMillis());
        USERS_BY_COOKIE.put(cookie, user);
        saveNewCookie(user, cookie, System.currentTimeMillis());
        activeCookieExpiryCleanupTask.addExpiry(cookie, System.currentTimeMillis() + cookieExpiresAfter);
        return cookie;
    }

    private void saveNewCookie(User user, String cookie, long now) {
        dbSystem.getDatabase().executeTransaction(CookieChangeTransaction.storeCookie(
                user.getUsername(), cookie, now + cookieExpiresAfter
        ));
    }

    public void removeCookie(String cookie) {
        Optional<User> foundUser = checkCookie(cookie);
        if (foundUser.isPresent()) {
            USERS_BY_COOKIE.remove(cookie);
            deleteCookie(foundUser.get().getUsername());
        }
    }

    private void deleteCookie(String username) {
        dbSystem.getDatabase().executeTransaction(CookieChangeTransaction.removeCookie(username));
    }

    public void removeAll() {
        disable();
        dbSystem.getDatabase().executeTransaction(CookieChangeTransaction.removeAll());
    }
}