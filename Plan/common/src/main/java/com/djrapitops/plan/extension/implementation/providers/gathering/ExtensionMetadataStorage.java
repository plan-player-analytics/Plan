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
package com.djrapitops.plan.extension.implementation.providers.gathering;

import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.implementation.ProviderInformation;
import com.djrapitops.plan.extension.implementation.providers.Parameters;
import com.djrapitops.plan.extension.implementation.storage.transactions.StoreIconTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreProviderTransaction;
import com.djrapitops.plan.extension.implementation.storage.transactions.providers.StoreTableProviderTransaction;
import com.djrapitops.plan.extension.table.Table;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Avoids writing unchanged extension metadata for every gathered value.
 */
@Singleton
public class ExtensionMetadataStorage {

    private static final Map<ExtensionMetadataKey, MetadataFingerprint> storedMetadata = new HashMap<>();

    private final DBSystem dbSystem;

    @Inject
    public ExtensionMetadataStorage(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
        storedMetadata.clear();
    }

    public static void invalidateAll() {
        storedMetadata.clear();
    }

    public synchronized void storeProvider(
            ProviderInformation information,
            Parameters parameters
    ) {
        Database database = dbSystem.getDatabase();
        ExtensionMetadataKey key = new ExtensionMetadataKey(false, parameters.getServerUUID(), information.getPluginName(), information.getName());
        MetadataFingerprint fingerprint = MetadataFingerprint.forProvider(information);
        if (fingerprint.equals(storedMetadata.get(key))) return;

        Transaction[] transactions = new Transaction[]{
                new StoreIconTransaction(information.getIcon()),
                new StoreProviderTransaction(information, parameters)
        };
        CompletableFuture.allOf(Arrays.stream(transactions)
                .map(database::executeTransaction)
                .toArray(CompletableFuture[]::new)
        ).thenRun(() -> {
            if (Arrays.stream(transactions).allMatch(Transaction::wasSuccessful)) {
                storedMetadata.put(key, fingerprint);
            }
        }).join();
    }

    public synchronized void storeTableProvider(
            ProviderInformation information,
            Parameters parameters,
            Table table
    ) {
        Database database = dbSystem.getDatabase();
        ExtensionMetadataKey key = new ExtensionMetadataKey(true, parameters.getServerUUID(), information.getPluginName(), information.getName());
        MetadataFingerprint fingerprint = MetadataFingerprint.forTableProvider(information, parameters, table);
        if (fingerprint.equals(storedMetadata.get(key))) return;

        List<Transaction> transactions = new ArrayList<>();
        for (Icon icon : table.getIcons()) {
            if (icon != null) {
                transactions.add(new StoreIconTransaction(icon));
            }
        }
        transactions.add(new StoreTableProviderTransaction(information, parameters, table));

        CompletableFuture.allOf(transactions.stream()
                .map(database::executeTransaction)
                .toArray(CompletableFuture[]::new)
        ).thenRun(() -> {
            if (transactions.stream().allMatch(Transaction::wasSuccessful)) {
                storedMetadata.put(key, fingerprint);
            }
        }).join();
    }

    public void invalidate(List<ExtensionMetadataKey> invalidatedProviders) {
        storedMetadata.keySet().removeAll(invalidatedProviders);
    }
}