import React from 'react';
import MultipleChoiceFilter from "./MultipleChoiceFilter";

const PluginGroupsFilter = ({index, filter, removeFilter, setFilterOptions}) => {
    const label = `are in ${filter.options.plugin}'s ${filter.options.group} Groups`

    return (
        <MultipleChoiceFilter
            index={index}
            label={label}
            filter={filter}
            removeFilter={removeFilter}
            setFilterOptions={setFilterOptions}
        />
    )
};

export default PluginGroupsFilter