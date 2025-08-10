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
package com.djrapitops.plan.delivery.domain.datatransfer;

import java.util.Map;
import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class ThemeDto {

    private String name;
    private Map<String, String> colors;
    private Map<String, String> nightColors;
    private Map<String, Object> useCases;
    private Map<String, Object> nightModeUseCases;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getColors() {
        return colors;
    }

    public void setColors(Map<String, String> colors) {
        this.colors = colors;
    }

    public Map<String, String> getNightColors() {
        return nightColors;
    }

    public void setNightColors(Map<String, String> nightColors) {
        this.nightColors = nightColors;
    }

    public Map<String, Object> getUseCases() {
        return useCases;
    }

    public void setUseCases(Map<String, Object> useCases) {
        this.useCases = useCases;
    }

    public Map<String, Object> getNightModeUseCases() {
        return nightModeUseCases;
    }

    public void setNightModeUseCases(Map<String, Object> nightModeUseCases) {
        this.nightModeUseCases = nightModeUseCases;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ThemeDto themeDto = (ThemeDto) o;
        return Objects.equals(getColors(), themeDto.getColors()) && Objects.equals(getNightColors(), themeDto.getNightColors()) && Objects.equals(getUseCases(), themeDto.getUseCases()) && Objects.equals(getNightModeUseCases(), themeDto.getNightModeUseCases());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColors(), getNightColors(), getUseCases(), getNightModeUseCases());
    }

    @Override
    public String toString() {
        return "ThemeDto{" +
                "colors=" + colors +
                ", nightColors=" + nightColors +
                ", useCases=" + useCases +
                ", nightModeUseCases=" + nightModeUseCases +
                '}';
    }
}
