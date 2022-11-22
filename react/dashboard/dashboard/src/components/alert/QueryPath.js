import React from 'react';
import {useQueryResultContext} from "../../hooks/queryResultContext";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faFilter} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";

const QueryPath = () => {
    const {t} = useTranslation();
    const {result} = useQueryResultContext();
    const hasResults = result && result.data;
    const path = result?.path;
    if (!path || !path.length) return <></>;


    const getReadableFilterName = kind => {
        if (kind.endsWith(" (skip)")) {
            return getReadableFilterName(kind.substring(0, kind.length - 5)) + " (" + t('html.query.filter.skipped') + ")";
        }

        if (kind.startsWith("pluginGroups-")) {
            return "Group: " + kind.substring(13);
        }
        switch (kind) {
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
                return kind.kind;
        }
    };

    return (
        <aside id={"result-path"} className={"alert shadow " + (hasResults ? "alert-success" : "alert-warning")}>
            {path.map((step, i) => <p key={i} style={{marginBottom: 0, marginLeft: i * 0.7 + "rem"}}>
                <FontAwesomeIcon
                    icon={faFilter}/> '{getReadableFilterName(step.kind)}' matched {step.size} players
            </p>)}
        </aside>
    )
};

export default QueryPath