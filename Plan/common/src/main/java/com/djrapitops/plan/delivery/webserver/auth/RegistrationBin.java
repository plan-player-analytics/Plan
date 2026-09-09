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

import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.sql.tables.webuser.RegistrationTable;
import com.djrapitops.plan.storage.database.transactions.webuser.StoreIncompleteRegistrationTransaction;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.codec.digest.DigestUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Holds registrations of users before they are confirmed.
 *
 * @author AuroraLS3
 */
@Singleton
public class RegistrationBin {

    private final DBSystem dbSystem;

    @Inject
    public RegistrationBin(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    public String addInfoForRegistration(@Untrusted String username, @Untrusted String password) {
        String hash = PassEncryptUtil.createHash(password);
        String code = DigestUtils.sha256Hex(username + hash + System.currentTimeMillis()).substring(0, 12);
        long expiresAfter = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15L);

        dbSystem.getDatabase().executeTransaction(new StoreIncompleteRegistrationTransaction(
                new IncompleteRegistration(username, hash, code, expiresAfter)
        ));
        return code;
    }

    public Optional<User> register(@Untrusted String code, UUID linkedToUUID) {
        return dbSystem.getDatabase().query(WebUserQueries.fetchIncompleteRegistration(code))
                .map(registration -> {
                    dbSystem.getDatabase().executeInTransaction(RegistrationTable.DELETE_BY_CODE, registration.getCode());
                    return registration.toUser(linkedToUUID);
                });
    }

    public boolean contains(@Untrusted String code) {
        // This method is used to check if registration was completed by the user.
        // There's a bug here where incomplete registration can be cleaned up before registration completes while register window is open
        // Which can lead to a confusing situation where user gets "Registration succeeded, you can now log in"-message
        // Even though the user was not actually registered.
        Optional<IncompleteRegistration> incompleteRegistration = dbSystem.getDatabase().query(WebUserQueries.fetchIncompleteRegistration(code));
        return incompleteRegistration.isPresent();
    }
}
