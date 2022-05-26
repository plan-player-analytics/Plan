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

import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.event.JoinAddress;
import com.djrapitops.plan.storage.database.queries.DataStoreQueries;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.UUID;

/**
 * Transaction for storing a session after a session has ended.
 *
 * @author AuroraLS3
 */
public class SessionEndTransaction extends Transaction {

    private final FinishedSession session;

    public SessionEndTransaction(FinishedSession session) {
        this.session = session;
    }

    @Override
    protected void performOperations() {
        try {
            storeSession();
        } catch (DBOpException failed) {
            if (failed.isUserIdConstraintViolation()) {
                retry(failed);
            } else {
                throw failed;
            }
        }
    }

    private void storeSession() {
        storeJoinAddressIfPresent();
        execute(DataStoreQueries.storeSession(session));
    }

    private void storeJoinAddressIfPresent() {
        session.getExtraData(JoinAddress.class)
                .map(JoinAddress::getAddress)
                .map(StoreJoinAddressTransaction::new)
                .ifPresent(this::executeOther);
    }

    private void retry(DBOpException failed) {
        try {
            UUID playerUUID = session.getPlayerUUID();
            String playerName = session.getExtraData(PlayerName.class)
                    .map(PlayerName::get)
                    .orElseGet(playerUUID::toString);
            executeOther(new PlayerRegisterTransaction(playerUUID, session::getStart, playerName));
            storeSession();
        } catch (DBOpException anotherFail) {
            anotherFail.addSuppressed(failed);
            throw anotherFail;
        }
    }
}