import React from 'react';
import MultipleChoiceFilter from "./MultipleChoiceFilter";
import {useTranslation} from "react-i18next";

const PluginGroupsFilter = ({index, filter, removeFilter, setFilterOptions}) => {
    const {t} = useTranslation();
    const label = t('html.query.filter.pluginGroup.text', {
        plugin: filter.options.plugin, group: filter.options.group
    });

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