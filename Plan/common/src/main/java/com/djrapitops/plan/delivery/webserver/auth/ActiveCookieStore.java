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
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.events.CookieChangeTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Singleton
public class ActiveCookieStore implements SubSystem {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final Map<String, CookieMetadata> usersByCookie = new ConcurrentHashMap<>();
    private static long cookieExpiresAfterMs = TimeUnit.HOURS.toMillis(2L);

    private final ActiveCookieExpiryCleanupTask activeCookieExpiryCleanupTask;

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Processing processing;
    private final PluginLogger logger;

    @Inject
    public ActiveCookieStore(
            ActiveCookieExpiryCleanupTask activeCookieExpiryCleanupTask,
            PlanConfig config,
            DBSystem dbSystem,
            Processing processing,
            PluginLogger logger
    ) {
        this.logger = logger;
        Holder.setActiveCookieStore(this);
        this.activeCookieExpiryCleanupTask = activeCookieExpiryCleanupTask;

        this.config = config;
        this.dbSystem = dbSystem;
        this.processing = processing;
    }

    public static long getCookieExpiresAfterMs() {
        return cookieExpiresAfterMs;
    }

    public void removeUserCookie(@Untrusted String username) {
        usersByCookie.entrySet().stream().filter(entry -> entry.getValue().getUser().getUsername().equals(username))
                .findAny()
                .map(Map.Entry::getKey)
                .ifPresent(this::removeCookie);
    }

    public static void removeUserCookieStatic(@Untrusted String username) {
        Holder.getActiveCookieStore().removeUserCookie(username);
    }

    private static void setCookiesExpireAfter(Long expireAfterMs) {
        cookieExpiresAfterMs = expireAfterMs;
    }

    @Override
    public void enable() {
        ActiveCookieStore.setCookiesExpireAfter(config.get(WebserverSettings.COOKIES_EXPIRE_AFTER));
        processing.submitNonCritical(this::reloadActiveCookies);
    }

    public void reloadActiveCookies() {
        try {
            Map<String, CookieMetadata> cookies = dbSystem.getDatabase().query(WebUserQueries.fetchActiveCookies());
            usersByCookie.clear();
            usersByCookie.putAll(cookies);
            for (Map.Entry<String, CookieMetadata> entry : cookies.entrySet()) {
                long timeToExpiry = Math.max(entry.getValue().getExpires() - System.currentTimeMillis(), 0L);
                activeCookieExpiryCleanupTask.addExpiry(entry.getKey(), System.currentTimeMillis() + timeToExpiry);
            }
        } catch (DBOpException databaseClosedUnexpectedly) {
            logger.info("Database closed unexpectedly so active cookies could not be loaded.");
            // Safe to ignore https://github.com/plan-player-analytics/Plan/issues/2188
        }
    }

    @Override
    public void disable() {
        /* no-op */
    }

    public Optional<CookieMetadata> findCookie(@Untrusted String cookie) {
        return Optional.ofNullable(usersByCookie.get(cookie));
    }

    public CompletableFuture<String> generateNewCookie(User user, String ipAddress) {
        String cookie = DigestUtils.sha256Hex(user.getUsername() + UUID.randomUUID() + System.currentTimeMillis() + SECURE_RANDOM.nextLong());
        long expiresAt = System.currentTimeMillis() + cookieExpiresAfterMs;
        usersByCookie.put(cookie, new CookieMetadata(user, expiresAt, ipAddress));
        CompletableFuture<Void> saveFuture = saveNewCookie(user, cookie, System.currentTimeMillis(), ipAddress);
        activeCookieExpiryCleanupTask.addExpiry(cookie, System.currentTimeMillis() + cookieExpiresAfterMs);
        return saveFuture.thenApply(v -> cookie);
    }

    private CompletableFuture<Void> saveNewCookie(User user, String cookie, long now, String ipAddress) {
        return dbSystem.getDatabase().executeTransaction(CookieChangeTransaction.storeCookie(
                user.getUsername(), cookie, now + cookieExpiresAfterMs, ipAddress
        ));
    }

    public void removeCookie(@Untrusted String cookie) {
        findCookie(cookie)
                .map(CookieMetadata::getUser)
                .map(User::getUsername)
                .ifPresent(this::deleteCookieByUser);
        usersByCookie.remove(cookie);
        deleteCookie(cookie);
    }

    private void deleteCookie(@Untrusted String cookie) {
        dbSystem.getDatabase().executeTransaction(CookieChangeTransaction.removeCookie(cookie));
    }

    private void deleteCookieByUser(String username) {
        dbSystem.getDatabase().executeTransaction(CookieChangeTransaction.removeCookieByUser(username));
    }

    public void removeAll() {
        usersByCookie.clear();
        dbSystem.getDatabase().executeTransaction(CookieChangeTransaction.removeAll());
    }

    public static class Holder {
        private static ActiveCookieStore activeCookieStore;

        private Holder() {}

        public static ActiveCookieStore getActiveCookieStore() {
            return activeCookieStore;
        }

        public static void setActiveCookieStore(ActiveCookieStore activeCookieStore) {
            Holder.activeCookieStore = activeCookieStore;
        }
    }
}