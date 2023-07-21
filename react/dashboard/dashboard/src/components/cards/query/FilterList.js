import React from 'react';
import Filter from "./filter/Filter";

const FilterList = ({filters, setFilters, setAsInvalid, setAsValid}) => {
    const updateFilterOptions = (index, newOptions) => {
        filters[index] = newOptions;
        setFilters(filters);
    }

    const removeFilter = index => {
        setFilters(filters.filter((f, i) => i !== index));
    }

    const moveUp = index => {
        if (index === 0) {
            return;
        }
        [filters[index - 1], filters[index]] = [filters[index], filters[index - 1]];
        setFilters(filters);
    }

    const moveDown = index => {
        if (index === filters.length - 1) {
            return;
        }
        [filters[index], filters[index + 1]] = [filters[index + 1], filters[index]];
        setFilters(filters);
    }

    return (
        <ul id={"filters"} className={"filters"}>
            {filters.map((filter, i) => <li key={'filter-' + i} className={"filter"}>
                <Filter filter={filter} index={i}
                        setFilterOptions={newOptions => updateFilterOptions(i, newOptions)}
                        removeFilter={() => removeFilter(i)}
                        moveUp={() => moveUp(i)}
                        moveDown={() => moveDown(i)}
                        setAsInvalid={setAsInvalid} setAsValid={setAsValid}
                />
            </li>)}
        </ul>
    )
};

export default FilterList