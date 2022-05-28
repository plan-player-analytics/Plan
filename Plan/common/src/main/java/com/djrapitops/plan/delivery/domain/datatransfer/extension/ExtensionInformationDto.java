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

import com.djrapitops.plan.extension.implementation.results.ExtensionInformation;

import java.util.Objects;

public class ExtensionInformationDto {

    private final String pluginName;
    private final IconDto icon;

    public ExtensionInformationDto(ExtensionInformation extensionInformation) {
        this.pluginName = extensionInformation.getPluginName();
        this.icon = new IconDto(extensionInformation.getIcon());
    }

    public String getPluginName() {
        return pluginName;
    }

    public IconDto getIcon() {
        return icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtensionInformationDto that = (ExtensionInformationDto) o;
        return Objects.equals(getPluginName(), that.getPluginName()) && Objects.equals(getIcon(), that.getIcon());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPluginName(), getIcon());
    }

    @Override
    public String toString() {
        return "ExtensionInformationDto{" +
                "pluginName='" + pluginName + '\'' +
                ", icon=" + icon +
                '}';
    }
}
