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

import com.djrapitops.plan.utilities.dev.Untrusted;

import java.util.List;
import java.util.Objects;

public class InputQueryDto {

    @Untrusted
    public final List<InputFilterDto> filters;
    private final ViewDto view;

    public InputQueryDto(ViewDto view, List<InputFilterDto> filters) {
        this.view = view;
        this.filters = filters;
    }

    public ViewDto getView() {
        return view;
    }

    @Untrusted
    public List<InputFilterDto> getFilters() {
        return filters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InputQueryDto that = (InputQueryDto) o;
        return Objects.equals(getView(), that.getView()) && Objects.equals(getFilters(), that.getFilters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getView(), getFilters());
    }

    @Override
    public String toString() {
        return "InputQueryDto{" +
                "view=" + view +
                ", filters=" + filters +
                '}';
    }
}
