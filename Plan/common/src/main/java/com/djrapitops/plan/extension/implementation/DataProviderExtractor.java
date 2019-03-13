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
package com.djrapitops.plan.extension.implementation;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.Tab;
import com.djrapitops.plan.extension.annotation.TabInfo;
import com.djrapitops.plan.extension.annotation.TabOrder;
import com.djrapitops.plan.extension.extractor.ExtensionExtractor;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Icon;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Extracts objects that can be used to obtain data to store in the database.
 * <p>
 * Goal of this class is to abstract away DataExtension API annotations so that they will not be needed outside when calling methods.
 *
 * @author Rsl1122
 */
public class DataProviderExtractor {

    private final DataExtension extension;
    private ExtensionExtractor extensionExtractor;

    /**
     * Create a DataProviderExtractor.
     *
     * @param extension DataExtension to extract information from.
     * @throws IllegalArgumentException If something is badly wrong with the specified extension class annotations.
     */
    public DataProviderExtractor(DataExtension extension) {
        this.extension = extension;
        extensionExtractor = new ExtensionExtractor(extension);

        extensionExtractor.extractAnnotationInformation();
    }

    public String getPluginName() {
        return extensionExtractor.getPluginInfo().name();
    }

    public Icon getPluginIcon() {
        PluginInfo pluginInfo = extensionExtractor.getPluginInfo();
        return new Icon(pluginInfo.iconFamily(), pluginInfo.iconName(), pluginInfo.color());
    }

    public Collection<PluginTab> getPluginTabs() {
        Map<String, TabInfo> tabInformation = extensionExtractor.getTabInformation()
                .stream().collect(Collectors.toMap(TabInfo::tab, Function.identity(), (one, two) -> one));

        return extensionExtractor.getMethodAnnotations().getAnnotations(Tab.class).stream()
                .map(Tab::value)
                .distinct()
                .map(tabName -> {
                    Optional<TabInfo> tabInfo = Optional.ofNullable(tabInformation.get(tabName));
                    return new PluginTab(
                            tabName,
                            tabInfo.map(info -> new Icon(info.iconFamily(), info.iconName(), Color.NONE)).orElse(null),
                            tabInfo.map(TabInfo::elementOrder).orElse(null)
                    );
                }).collect(Collectors.toList());
    }

    public Optional<String[]> getTabOrder() {
        return extensionExtractor.getTabOrder().map(TabOrder::value);
    }
}