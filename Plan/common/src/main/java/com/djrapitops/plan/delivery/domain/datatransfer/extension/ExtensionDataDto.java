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
package com.djrapitops.plan.delivery.domain.datatransfer.extension;

import com.djrapitops.plan.extension.implementation.results.ExtensionData;

import java.util.List;

public record ExtensionDataDto(
        ExtensionInformationDto extensionInformation,
        List<ExtensionTabDataDto> tabs,
        boolean onlyGenericTab,
        boolean wide
) {

    public ExtensionDataDto(ExtensionData extensionData) {
        this(
                new ExtensionInformationDto(extensionData.getExtensionInformation()),
                extensionData.getTabs().stream().map(ExtensionTabDataDto::new).toList(),
                extensionData.hasOnlyGenericTab(),
                extensionData.doesNeedWiderSpace()
        );
    }
}
