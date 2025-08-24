import React from 'react';
import MultipleChoiceFilter from "./MultipleChoiceFilter";
import {useTranslation} from "react-i18next";
import PluginGroupsFilter from "./PluginGroupsFilter";
import BetweenDatesFilter from "./BetweenDatesFilter";

const Filter = ({index, filter, setFilterOptions, removeFilter, setAsInvalid, setAsValid}) => {
    const {t} = useTranslation();

    if (filter.kind.startsWith("pluginGroups-")) {
        return <PluginGroupsFilter index={index} filter={filter}
                                   setFilterOptions={setFilterOptions} removeFilter={removeFilter}/>;
    }

    const are = t('html.query.generic.are')
        .replaceAll("`", "");
    switch (filter.kind) {
        case "activityIndexNow":
            return <MultipleChoiceFilter index={index} filter={filter} label={t('html.query.filter.activity.text')}
                                         setFilterOptions={setFilterOptions} removeFilter={removeFilter}/>;
        case "allPlayers":
        case "banned":
        case "operators":
            return <MultipleChoiceFilter index={index} filter={filter} label={are}
                                         setFilterOptions={setFilterOptions} removeFilter={removeFilter}/>
        case "joinAddresses":
            return <MultipleChoiceFilter index={index} filter={filter} label={t('html.query.filter.joinAddress.text')}
                                         setFilterOptions={setFilterOptions} removeFilter={removeFilter}/>
        case "geolocations":
            return <MultipleChoiceFilter index={index} filter={filter} label={t('html.query.filter.country.text')}
                                         setFilterOptions={setFilterOptions} removeFilter={removeFilter}/>
        case "playedOnServer":
            return <MultipleChoiceFilter index={index} filter={filter}
                                         label={t('html.query.filter.hasPlayedOnServers.text')}
                                         setFilterOptions={setFilterOptions} removeFilter={removeFilter}/>
        case "pluginsBooleanGroups":
            return <MultipleChoiceFilter index={index} filter={filter}
                                         label={t('html.query.filter.hasPluginBooleanValue.text')}
                                         setFilterOptions={setFilterOptions} removeFilter={removeFilter}/>
        case "playedBetween":
            return <BetweenDatesFilter index={index} filter={filter}
                                       label={t('html.query.filter.playedBetween.text')}
                                       setFilterOptions={setFilterOptions} removeFilter={removeFilter}
                                       setAsInvalid={setAsInvalid} setAsValid={setAsValid}/>
        case "registeredBetween":
            return <BetweenDatesFilter index={index} filter={filter}
                                       label={t('html.query.filter.registeredBetween.text')}
                                       setFilterOptions={setFilterOptions} removeFilter={removeFilter}
                                       setAsInvalid={setAsInvalid} setAsValid={setAsValid}/>
        default:
            return (
                <div className={"my-2"}>
                    <p>Unknown filter {filter.kind}</p>
                </div>
            )
    }

};

export default Filter