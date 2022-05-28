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
import java.util.Objects;
import java.util.stream.Collectors;

public class ExtensionDataDto {

    private final ExtensionInformationDto extensionInformation;
    private final List<ExtensionTabDataDto> tabs;

    private final boolean onlyGenericTab;
    private final boolean wide;

    public ExtensionDataDto(ExtensionData extensionData) {
        this.extensionInformation = new ExtensionInformationDto(extensionData.getExtensionInformation());
        this.tabs = extensionData.getTabs().stream().map(ExtensionTabDataDto::new).collect(Collectors.toList());

        onlyGenericTab = extensionData.hasOnlyGenericTab();
        wide = extensionData.doesNeedWiderSpace();
    }

    public ExtensionInformationDto getExtensionInformation() {
        return extensionInformation;
    }

    public List<ExtensionTabDataDto> getTabs() {
        return tabs;
    }

    public boolean isOnlyGenericTab() {
        return onlyGenericTab;
    }

    public boolean isWide() {
        return wide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionDataDto that = (ExtensionDataDto) o;
        return Objects.equals(getExtensionInformation(), that.getExtensionInformation()) && Objects.equals(getTabs(), that.getTabs());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExtensionInformation(), getTabs());
    }

    @Override
    public String toString() {
        return "ExtensionDataDto{" +
                "extensionInformation=" + extensionInformation +
                ", tabs=" + tabs +
                '}';
    }
}
