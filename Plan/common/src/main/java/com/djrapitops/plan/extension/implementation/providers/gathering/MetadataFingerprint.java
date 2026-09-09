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
import com.djrapitops.plan.extension.table.Table;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class MetadataFingerprint {
    private final String hash;

    private MetadataFingerprint(String hash) {
        this.hash = hash;
    }

    public static MetadataFingerprint forProvider(ProviderInformation information) {
        return new MetadataFingerprint(DigestUtils.sha256Hex(providerInformation(information).toString()));
    }

    public static MetadataFingerprint forTableProvider(
            ProviderInformation information,
            Parameters parameters,
            Table table
    ) {
        List<Object> values = providerInformation(information);
        values.add(parameters.getMethodType());
        values.addAll(Arrays.asList(table.getColumns()));
        for (Icon icon : table.getIcons()) {
            addIcon(values, icon);
        }
        values.addAll(Arrays.asList(table.getTableColumnFormats()));
        return new MetadataFingerprint(DigestUtils.sha256Hex(values.toString()));
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
        return hash.equals(that.hash);
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    @Override
    public String toString() {
        return "MetadataFingerprint{" +
                "hash='" + hash + '\'' +
                '}';
    }
}
