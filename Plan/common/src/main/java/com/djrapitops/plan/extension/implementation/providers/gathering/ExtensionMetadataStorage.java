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
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.Transaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Avoids writing unchanged extension metadata for every gathered value.
 */
class ExtensionMetadataStorage {

    private final Map<MetadataKey, MetadataFingerprint> storedMetadata = new HashMap<>();

    public synchronized void storeProvider(
            Database database,
            ProviderInformation information,
            Parameters parameters
    ) {
        MetadataKey key = new MetadataKey(false, parameters.getServerUUID(), information.getPluginName(), information.getName());
        MetadataFingerprint fingerprint = MetadataFingerprint.forProvider(information);
        if (fingerprint.equals(storedMetadata.get(key))) return;

        storedMetadata.put(key, fingerprint);
        StoreProviderTransaction providerTransaction = new StoreProviderTransaction(information, parameters);
        try {
            database.executeTransaction(new StoreIconTransaction(information.getIcon()));
            invalidateIfNotStored(database.executeTransaction(providerTransaction), providerTransaction, key, fingerprint);
        } catch (RuntimeException executionFailure) {
            storedMetadata.remove(key, fingerprint);
            throw executionFailure;
        }
    }

    public synchronized void storeTableProvider(
            Database database,
            ProviderInformation information,
            Parameters parameters,
            Table table
    ) {
        MetadataKey key = new MetadataKey(true, parameters.getServerUUID(), information.getPluginName(), information.getName());
        MetadataFingerprint fingerprint = MetadataFingerprint.forTableProvider(information, parameters, table);
        if (fingerprint.equals(storedMetadata.get(key))) return;

        storedMetadata.put(key, fingerprint);
        StoreTableProviderTransaction providerTransaction = new StoreTableProviderTransaction(information, parameters, table);
        try {
            Icon[] icons = table.getIcons();
            for (Icon icon : icons) {
                if (icon != null) {
                    database.executeTransaction(new StoreIconTransaction(icon));
                }
            }
            invalidateIfNotStored(database.executeTransaction(providerTransaction), providerTransaction, key, fingerprint);
        } catch (RuntimeException executionFailure) {
            storedMetadata.remove(key, fingerprint);
            throw executionFailure;
        }
    }

    private void invalidateIfNotStored(
            CompletableFuture<?> completion,
            Transaction transaction,
            MetadataKey key,
            MetadataFingerprint fingerprint
    ) {
        completion.whenComplete((result, failure) -> {
            if (failure != null || !transaction.wasExecuted()) {
                synchronized (ExtensionMetadataStorage.this) {
                    storedMetadata.remove(key, fingerprint);
                }
            }
        });
    }

    private static final class MetadataKey {
        private final boolean tableProvider;
        private final ServerUUID serverUUID;
        private final String pluginName;
        private final String providerName;

        private MetadataKey(boolean tableProvider, ServerUUID serverUUID, String pluginName, String providerName) {
            this.tableProvider = tableProvider;
            this.serverUUID = serverUUID;
            this.pluginName = pluginName;
            this.providerName = providerName;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof MetadataKey)) return false;
            MetadataKey that = (MetadataKey) other;
            return tableProvider == that.tableProvider &&
                    Objects.equals(serverUUID, that.serverUUID) &&
                    Objects.equals(pluginName, that.pluginName) &&
                    Objects.equals(providerName, that.providerName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tableProvider, serverUUID, pluginName, providerName);
        }
    }

    private static final class MetadataFingerprint {
        private final List<Object> values;

        private MetadataFingerprint(List<Object> values) {
            this.values = values;
        }

        private static MetadataFingerprint forProvider(ProviderInformation information) {
            return new MetadataFingerprint(providerInformation(information));
        }

        private static MetadataFingerprint forTableProvider(
                ProviderInformation information,
                Parameters parameters,
                Table table
        ) {
            List<Object> values = providerInformation(information);
            values.add(parameters.getMethodType());
            values.addAll(Arrays.asList(table.getColumns().clone()));
            for (Icon icon : table.getIcons()) {
                addIcon(values, icon);
            }
            values.addAll(Arrays.asList(table.getTableColumnFormats().clone()));
            return new MetadataFingerprint(values);
        }

        private static List<Object> providerInformation(ProviderInformation information) {
            List<Object> values = new ArrayList<>();
            values.add(information.getPluginName());
            values.add(information.getName());
            values.add(information.getText());
            values.add(information.getDescription().orElse(null));
            values.add(information.getPriority());
            addIcon(values, information.getIcon());
            values.add(information.isShownInPlayersTable());
            values.add(information.getTab().orElse(null));
            values.add(information.getCondition().orElse(null));
            values.add(information.isHidden());
            values.add(information.getProvidedCondition());
            values.add(information.getFormatType().orElse(null));
            values.add(information.isPlayerName());
            values.add(information.getTableColor());
            values.add(information.isPercentage());
            values.add(information.isComponent());
            return values;
        }

        private static void addIcon(List<Object> values, Icon icon) {
            if (icon == null) {
                values.add(null);
                values.add(null);
                values.add(null);
                return;
            }
            values.add(icon.getFamily());
            values.add(icon.getName());
            values.add(icon.getColor());
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof MetadataFingerprint)) return false;
            MetadataFingerprint that = (MetadataFingerprint) other;
            return values.equals(that.values);
        }

        @Override
        public int hashCode() {
            return values.hashCode();
        }
    }
}
