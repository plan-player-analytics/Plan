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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestConstants;

import java.lang.reflect.Field;
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
    @Mock
    DBSystem dbSystem;

    private ExtensionMetadataStorage underTest;
    private Parameters parameters;
    private List<Transaction> submitted;
    private Map<Transaction, CompletableFuture<Object>> completions;

    @BeforeEach
    void setUp() {
        when(database.executeTransaction(any())).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            CompletableFuture<Object> completion = new CompletableFuture<>();
            submitted.add(transaction);
            completions.put(transaction, completion);
            return completion;
        });
        when(dbSystem.getDatabase()).thenReturn(database);

        underTest = new ExtensionMetadataStorage(dbSystem);
        parameters = Parameters.server(TestConstants.SERVER_UUID);
        submitted = new ArrayList<>();
        completions = new IdentityHashMap<>();
    }

    private void transactionsSucceed() {
        submitted.forEach(transaction -> {
            try {
                // Mock transaction success
                Field success = Transaction.class.getDeclaredField("success");
                success.setAccessible(true);
                success.set(transaction, true);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        completions.values().forEach(completion -> completion.complete(null));
    }

    private void transactionsFail() {
        submitted.forEach(transaction -> {
            try {
                // Mock transaction success
                Field success = Transaction.class.getDeclaredField("success");
                success.setAccessible(true);
                success.set(transaction, false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        completions.values().forEach(completion -> completion.completeExceptionally(new RuntimeException()));
    }

    @Test
    void unchangedProviderMetadataIsOnlySubmittedOnce() {
        ProviderInformation information = provider("Online", "Online players");

        underTest.storeProvider(information, parameters);
        transactionsSucceed();
        underTest.storeProvider(information, parameters);
        transactionsSucceed();

        assertEquals(1, count(StoreIconTransaction.class));
        assertEquals(1, count(StoreProviderTransaction.class));
    }

    @Test
    void changedProviderMetadataIsSubmittedAgain() {
        underTest.storeProvider(provider("Online", "Online players"), parameters);
        transactionsSucceed();
        underTest.storeProvider(provider("Online", "Players online now"), parameters);
        transactionsSucceed();

        assertEquals(2, count(StoreIconTransaction.class));
        assertEquals(2, count(StoreProviderTransaction.class));
    }

    @Test
    void providerMetadataIsStoredForEachServer() {
        ProviderInformation information = provider("Online", "Online players");

        underTest.storeProvider(information, parameters);
        transactionsSucceed();
        underTest.storeProvider(information, Parameters.server(TestConstants.SERVER_TWO_UUID));
        transactionsSucceed();

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

        underTest.storeTableProvider(information, parameters, first);
        transactionsSucceed();
        underTest.storeTableProvider(information, parameters, second);
        transactionsSucceed();

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

        underTest.storeTableProvider(information, parameters, first);
        transactionsSucceed();
        underTest.storeTableProvider(information, parameters, second);
        transactionsSucceed();

        assertEquals(2, count(StoreIconTransaction.class));
        assertEquals(2, count(StoreTableProviderTransaction.class));
    }

    @Test
    void sparseTableColumnsStoreAllDefinedIcons() {
        ProviderInformation information = provider("Top players", "Top players");
        Table table = Table.builder()
                .columnOne("First", Icon.called("gavel").build())
                .columnTwo("Second", Icon.called("what").build())
                .columnThree("Third", Icon.called("question").build())
                .columnFive("Fifth", Icon.called("").build())
                .addRow("value", 3, 0.5)
                .build();

        underTest.storeTableProvider(information, parameters, table);
        transactionsSucceed();

        assertEquals(4, count(StoreIconTransaction.class));
        assertEquals(1, count(StoreTableProviderTransaction.class));
    }

    @Test
    void failedMetadataTransactionIsNotCached() {
        ProviderInformation information = provider("Online", "Online players");
        underTest.storeProvider(information, parameters);
        transactionsFail();
        underTest.storeProvider(information, parameters);
        transactionsSucceed();

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