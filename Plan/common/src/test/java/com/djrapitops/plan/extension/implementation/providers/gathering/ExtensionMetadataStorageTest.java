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
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestConstants;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtensionMetadataStorageTest {

    @Mock
    Database database;

    private ExtensionMetadataStorage underTest;
    private Parameters parameters;
    private List<Transaction> submitted;
    private Map<Transaction, CompletableFuture<Object>> completions;

    @BeforeEach
    void setUp() {
        underTest = new ExtensionMetadataStorage();
        parameters = Parameters.server(TestConstants.SERVER_UUID);
        submitted = new ArrayList<>();
        completions = new IdentityHashMap<>();
        when(database.executeTransaction(any())).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            CompletableFuture<Object> completion = new CompletableFuture<>();
            submitted.add(transaction);
            completions.put(transaction, completion);
            return completion;
        });
    }

    @Test
    void unchangedProviderMetadataIsOnlySubmittedOnce() {
        ProviderInformation information = provider("Online", "Online players");

        underTest.storeProvider(database, information, parameters);
        underTest.storeProvider(database, information, parameters);

        assertEquals(1, count(StoreIconTransaction.class));
        assertEquals(1, count(StoreProviderTransaction.class));
    }

    @Test
    void changedProviderMetadataIsSubmittedAgain() {
        underTest.storeProvider(database, provider("Online", "Online players"), parameters);
        underTest.storeProvider(database, provider("Online", "Players online now"), parameters);

        assertEquals(2, count(StoreIconTransaction.class));
        assertEquals(2, count(StoreProviderTransaction.class));
    }

    @Test
    void changingOnlyTableRowsDoesNotRewriteTableMetadata() {
        ProviderInformation information = provider("Top players", "Top players");
        Table first = Table.builder()
                .columnOne("Player", Icon.called("user").build())
                .addRow("Alice")
                .build();
        Table second = Table.builder()
                .columnOne("Player", Icon.called("user").build())
                .addRow("Bob")
                .build();

        underTest.storeTableProvider(database, information, parameters, first);
        underTest.storeTableProvider(database, information, parameters, second);

        assertEquals(1, count(StoreIconTransaction.class));
        assertEquals(1, count(StoreTableProviderTransaction.class));
    }

    @Test
    void changedTableColumnsRewriteTableMetadata() {
        ProviderInformation information = provider("Top players", "Top players");
        Table first = Table.builder()
                .columnOne("Player", Icon.called("user").build())
                .addRow("Alice")
                .build();
        Table second = Table.builder()
                .columnOne("Player name", Icon.called("user").build())
                .addRow("Alice")
                .build();

        underTest.storeTableProvider(database, information, parameters, first);
        underTest.storeTableProvider(database, information, parameters, second);

        assertEquals(2, count(StoreIconTransaction.class));
        assertEquals(2, count(StoreTableProviderTransaction.class));
    }

    @Test
    void skippedOrFailedMetadataTransactionIsNotCached() {
        ProviderInformation information = provider("Online", "Online players");
        underTest.storeProvider(database, information, parameters);
        Transaction providerTransaction = submitted.stream()
                .filter(StoreProviderTransaction.class::isInstance)
                .findFirst()
                .orElseThrow();

        completions.get(providerTransaction).complete(null);
        underTest.storeProvider(database, information, parameters);

        assertEquals(2, count(StoreIconTransaction.class));
        assertEquals(2, count(StoreProviderTransaction.class));
    }

    private ProviderInformation provider(String name, String text) {
        return ProviderInformation.builder("TestExtension")
                .setName(name)
                .setText(text)
                .setIcon(Icon.called("cube").build())
                .build();
    }

    private long count(Class<? extends Transaction> type) {
        return submitted.stream().filter(type::isInstance).count();
    }
}
