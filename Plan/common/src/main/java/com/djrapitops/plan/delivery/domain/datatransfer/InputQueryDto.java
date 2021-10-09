package com.djrapitops.plan.delivery.domain.datatransfer;

import java.util.List;
import java.util.Objects;

public class InputQueryDto {

    public final List<InputFilterDto> filters;
    private final ViewDto view;

    public InputQueryDto(ViewDto view, List<InputFilterDto> filters) {
        this.view = view;
        this.filters = filters;
    }

    public ViewDto getView() {
        return view;
    }

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
