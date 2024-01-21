import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import React, {useCallback, useEffect, useState} from "react";
import {faCheck, faGlobe, faSignal, faUser, faUserPlus, faUsers} from "@fortawesome/free-solid-svg-icons";
import DataTablesTable from "../../table/DataTablesTable";
import {CardLoader} from "../../navigation/Loader";
import {Link} from "react-router-dom";
import {faCalendarCheck, faCalendarPlus, faClock} from "@fortawesome/free-regular-svg-icons";
import {formatDate, useDatePreferences} from "../../text/FormattedDate";
import {useTimePreferences} from "../../text/FormattedTime";
import ExtensionIcon from "../../extensions/ExtensionIcon";
import {ExtensionValueTableCell} from "../../extensions/ExtensionCard";
import {usePreferences} from "../../../hooks/preferencesHook";
import {formatDecimals} from "../../../util/formatters";
import {formatTimeAmount} from "../../../util/format/TimeAmountFormat.js";

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

const PlayerListCard = ({data, title, justList, orderBy}) => {
    const {t} = useTranslation();
    const {preferencesLoaded, decimalFormat} = usePreferences();

    const [options, setOptions] = useState(undefined);

    const timePreferences = useTimePreferences();
    const datePreferences = useDatePreferences();

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

        const formatDateEasy = date => {
            return formatDate(date, datePreferences.offset, datePreferences.pattern, false, datePreferences.recentDaysPattern, t);
        }

        const rows = data.players.map(player => {
            const row = {
                name: player.playerName,
                uuid: player.playerUUID,
                link: <Link to={"/player/" + player.playerUUID}>{player.playerName}</Link>,
                activityIndex: player.activityIndex,
                activityGroup: t(getActivityGroup(player.activityIndex)),
                activityIndexAndGroup: formatDecimals(player.activityIndex, decimalFormat) + " (" + t(getActivityGroup(player.activityIndex)) + ")",
                activePlaytime: player.playtimeActive,
                activePlaytimeFormatted: formatTimeAmount(timePreferences, player.playtimeActive),
                sessions: player.sessionCount,
                registered: player.registered,
                registeredFormatted: formatDateEasy(player.registered),
                lastSeen: player.lastSeen,
                lastSeenFormatted: formatDateEasy(player.lastSeen),
                country: player.country,
                pingAverage: player.pingAverage,
                pingAverageFormatted: formatDecimals(player.pingAverage, decimalFormat) + "ms",
                pingMax: player.pingMax,
                pingMaxFormatted: player.pingMax + "ms",
                pingMin: player.pingMin,
                pingMinFormatted: player.pingMin + "ms"
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
    if (!options) return <CardLoader/>;

    if (justList) {
        return (
            <DataTablesTable id={"players-table"} rowKeyFunction={rowKeyFunction} options={options}/>
        );
    }

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faUsers} className="col-black"/> {title ? title : t('html.label.playerList')}
                </h6>
            </Card.Header>
            <DataTablesTable id={"players-table"}
                             rowKeyFunction={rowKeyFunction}
                             options={options}/>
        </Card>
    )
}

export default PlayerListCard;