import React from 'react';
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {Dropdown} from "react-bootstrap";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import Scrollable from "../../Scrollable";

const FilterDropdown = ({filterOptions, filters, setFilters}) => {
    const {t} = useTranslation();

    const addFilter = filter => {
        setFilters([...filters, filter])
    }

    const getReadableFilterName = filter => {
        if (filter.kind.startsWith("pluginGroups-")) {
            return t('html.query.filter.pluginGroup.name') + filter.kind.substring(13);
        }
        switch (filter.kind) {
            case "allPlayers":
                return t('html.query.filter.generic.allPlayers')
            case "activityIndexNow":
                return t('html.query.filter.title.activityGroup');
            case "banned":
                return t('html.query.filter.banStatus.name');
            case "operators":
                return t('html.query.filter.operatorStatus.name');
            case "joinAddresses":
                return t('html.label.joinAddresses');
            case "geolocations":
                return t('html.label.geolocations');
            case "playedBetween":
                return t('html.query.filter.playedBetween.text');
            case "registeredBetween":
                return t('html.query.filter.registeredBetween.text');
            case "pluginsBooleanGroups":
                return t('html.query.filter.hasPluginBooleanValue.name');
            case "playedOnServer":
                return t('html.query.filter.hasPlayedOnServers.name');
            default:
                return filter.kind;
        }
    };

    return (
        <Dropdown>
            <Dropdown.Toggle variant='' style={{'--bs-btn-color': 'var(--color-forms-input-text)'}}>
                <Fa icon={faPlus}/> {t('html.query.filters.add')}
            </Dropdown.Toggle>

            <Dropdown.Menu popperConfig={{strategy: "absolute"}}>
                <h6 className="dropdown-header">{t('html.query.filters.add')}</h6>
                <Scrollable>
                    {filterOptions.map(option => (
                        <Dropdown.Item key={JSON.stringify(option)} onClick={() => addFilter(option)}>
                            {getReadableFilterName(option)}
                        </Dropdown.Item>
                    ))}
                </Scrollable>
            </Dropdown.Menu>
        </Dropdown>
    )
};

export default FilterDropdown