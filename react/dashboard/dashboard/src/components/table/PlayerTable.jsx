import {useTranslation} from "react-i18next";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import React, {useCallback, useEffect, useState} from "react";
import FormattedTime from "../text/FormattedTime.jsx";
import FormattedDate from "../text/FormattedDate.jsx";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCheck, faGlobe, faSignal, faUser, faUserPlus} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck, faCalendarPlus, faClock} from "@fortawesome/free-regular-svg-icons";
import ExtensionIcon from "../extensions/ExtensionIcon.jsx";
import {Link} from "react-router-dom";
import {formatDecimals} from "../../util/formatters.js";
import {ExtensionValueTableCell} from "../extensions/ExtensionCard.jsx";
import {ChartLoader} from "../navigation/Loader.jsx";
import DataTablesTable from "./DataTablesTable.jsx";
import {localeService, reverseRegionLookupMap} from "../../service/localeService.js";

const getActivityGroup = value => {
    const VERY_ACTIVE = 3.75;
    const ACTIVE = 3.0;
    const REGULAR = 2.0;
    const IRREGULAR = 1.0;
    if (value >= VERY_ACTIVE) {
        return "html.label.veryActive"
    } else if (value >= ACTIVE) {
        return "html.label.active"
    } else if (value >= REGULAR) {
        return "html.label.indexRegular"
    } else if (value >= IRREGULAR) {
        return "html.label.irregular"
    } else {
        return "html.label.indexInactive"
    }
}

const PlayerTable = ({data, orderBy}) => {
    const {t} = useTranslation();
    const {preferencesLoaded, decimalFormat} = usePreferences();

    const [options, setOptions] = useState(undefined);

    useEffect(() => {
        if (!data) return;

        const columns = [{
            title: <><Fa icon={faUser}/> {t('html.label.name')}</>,
            data: {_: "name", display: "link"}
        }, {
            title: <><Fa icon={faCheck}/> {t('html.label.activityIndex')}</>,
            data: {_: "activityIndex", display: "activityIndexAndGroup"}
        }, {
            title: <><Fa icon={faClock}/> {t('html.label.activePlaytime')}</>,
            data: {_: "activePlaytime", display: "activePlaytimeFormatted"}
        }, {
            title: <><Fa icon={faCalendarPlus}/> {t('html.label.sessions')}</>,
            data: "sessions"
        }, {
            title: <><Fa icon={faUserPlus}/> {t('html.label.registered')}</>,
            data: {_: "registered", display: "registeredFormatted"}
        }, {
            title: <><Fa icon={faCalendarCheck}/> {t('html.label.lastSeen')}</>,
            data: {_: "lastSeen", display: "lastSeenFormatted"}
        }, {
            title: <><Fa icon={faGlobe}/> {t('html.label.country')}</>,
            data: "country"
        }, {
            title: <><Fa icon={faSignal}/> {t('html.label.averagePing')}</>,
            data: {_: "pingAverage", display: "pingAverageFormatted"}
        }, {
            title: <><Fa icon={faSignal}/> {t('html.label.bestPing')}</>,
            data: {_: "pingMin", display: "pingMinFormatted"}
        }, {
            title: <><Fa icon={faSignal}/> {t('html.label.worstPing')}</>,
            data: {_: "pingMax", display: "pingMaxFormatted"}
        }];

        columns.push(...data.extensionDescriptors.map(descriptor => {
            return {
                title: <><ExtensionIcon icon={descriptor.icon}/> {descriptor.text}</>,
                data: {_: descriptor.name + "Value", display: descriptor.name}
            }
        }));
        const regions = new Intl.DisplayNames([localeService.getIntlFriendlyLocale()], {type: 'region'});

        const rows = data.players.map(player => {
            const code = reverseRegionLookupMap[player.country];
            const location = code ? regions.of(code) : player.country?.replace('Local Machine', t('html.value.localMachine'));
            const row = {
                name: player.playerName,
                uuid: player.playerUUID,
                link: <Link to={"/player/" + player.playerUUID}>{player.playerName}</Link>,
                activityIndex: player.activityIndex,
                activityGroup: t(getActivityGroup(player.activityIndex)),
                activityIndexAndGroup: formatDecimals(player.activityIndex, decimalFormat) + " (" + t(getActivityGroup(player.activityIndex)) + ")",
                activePlaytime: player.playtimeActive,
                activePlaytimeFormatted: <FormattedTime timeMs={player.playtimeActive}/>,
                sessions: player.sessionCount,
                registered: player.registered,
                registeredFormatted: <FormattedDate date={player.registered} react/>,
                lastSeen: player.lastSeen,
                lastSeenFormatted: <FormattedDate date={player.lastSeen} react/>,
                country: location,
                pingAverage: player.pingAverage,
                pingAverageFormatted: localeService.localizePing(formatDecimals(player.pingAverage, decimalFormat)),
                pingMax: player.pingMax,
                pingMaxFormatted: localeService.localizePing(player.pingMax),
                pingMin: player.pingMin,
                pingMinFormatted: localeService.localizePing(player.pingMin)
            };
            data.extensionDescriptors.forEach(descriptor => {
                row[descriptor.name] = <ExtensionValueTableCell data={player.extensionValues[descriptor.name]}/>;
                row[descriptor.name + "Value"] = JSON.stringify(player.extensionValues[descriptor.name]?.value);
            })
            return row;
        });

        setOptions({
            responsive: true,
            deferRender: true,
            columns: columns,
            data: rows,
            order: [[orderBy !== undefined ? orderBy : 5, "desc"]]
        });
    }, [data, orderBy, t, decimalFormat]);

    const rowKeyFunction = useCallback((row, column) => {
        return row.uuid + "-" + (column ? JSON.stringify(column.data) : '');
    }, []);

    if (!preferencesLoaded) return <></>;
    if (!options) return <ChartLoader/>;

    return (
        <DataTablesTable id={"players-table"} rowKeyFunction={rowKeyFunction} options={options}/>
    );
}

export default PlayerTable;