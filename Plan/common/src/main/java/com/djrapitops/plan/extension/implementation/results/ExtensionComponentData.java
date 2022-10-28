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
package com.djrapitops.plan.extension.implementation.results;

import com.djrapitops.plan.component.Component;
import com.djrapitops.plan.component.ComponentOperation;
import com.djrapitops.plan.component.ComponentSvc;
import com.djrapitops.plan.delivery.rendering.html.Html;

/**
 * Represents double data returned by a DoubleProvider or PercentageProvider method.
 *
 * @author AuroraLS3
 */
public class ExtensionComponentData implements DescribedExtensionData {

    private final ExtensionDescription description;
    private final String value;

    public ExtensionComponentData(ExtensionDescription description, String value) {
        this.description = description;
        this.value = value;
    }

    public ExtensionDescription getDescription() {
        return description;
    }

    public String getFormattedValue() {
        return value;
    }

    public String getHtmlValue(ComponentSvc service) {
        String legacy = service.convert(service.fromJson(value), ComponentOperation.LEGACY, Component.SECTION);
        return Html.swapColorCodesToSpan(legacy);
    }
}