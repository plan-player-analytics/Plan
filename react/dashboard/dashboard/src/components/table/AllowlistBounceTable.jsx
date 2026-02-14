import React, {useCallback} from "react";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faDoorOpen, faRepeat, faUser} from "@fortawesome/free-solid-svg-icons";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import DataTablesTable from "./DataTablesTable.jsx";
import FormattedDate from "../text/FormattedDate.jsx";
import {faCalendarCheck, faCalendarTimes} from "@fortawesome/free-regular-svg-icons";
import {Link} from "react-router";

const AllowlistBounceTable = ({bounces, lastSeen}) => {
    const {t} = useTranslation();
    const {preferencesLoaded} = usePreferences();

    const columns = [{
        title: <><Fa icon={faUser}/> {t('html.label.player')}</>,
        data: {_: "player", display: "link"}
    }, {
        title: <><Fa icon={faRepeat}/> {t('html.label.attempts')}</>,
        data: "attempts"
    }, {
        title: <><Fa icon={faDoorOpen}/> {t('html.label.lastKnownAttempt')}</>,
        data: "lastKnownAttempt"
    }, {
        title: <><Fa icon={faCalendarTimes}/> {t('html.label.lastBlocked')}</>,
        data: {_: "date", display: "dateFormatted"}
    }, {
        title: <><Fa icon={faCalendarCheck}/> {t('html.label.lastAllowed')}</>,
        data: {_: "lastSeen", display: "lastSeenFormatted"}
    }];

    const rows = bounces.map(bounce => {
        const seenAfterBounce = bounce.lastBounce < lastSeen[bounce.playerUUID];
        const playerId = bounce.playerName + ' / ' + bounce.playerUUID;
        return {
            player: playerId,
            link: lastSeen[bounce.playerUUID] ? <Link to={"/player/" + bounce.playerUUID}>{playerId}</Link> : playerId,
            date: bounce.lastTime,
            dateFormatted: <FormattedDate date={bounce.lastTime} react/>,
            attempts: bounce.count,
            lastKnownAttempt: seenAfterBounce ? t('html.label.allowed') : t('html.label.blocked'),
            lastSeen: lastSeen[bounce.playerUUID],
            lastSeenFormatted: <FormattedDate date={lastSeen[bounce.playerUUID]} react/>
        };
    });
    const options = {
        responsive: true,
        deferRender: true,
        columns: columns,
        data: rows,
        paginationCount: 2,
        order: [[1, "desc"]]
    }

    const rowKeyFunction = useCallback((row, column) => {
        return row.player + "-" + (column ? JSON.stringify(column.data) : '');
    }, []);

    if (!preferencesLoaded) return <></>;

    return (
        <DataTablesTable id={"allowlist-bounce-table"} options={options} colorClass={"bg-allow-list"}
                         rowKeyFunction={rowKeyFunction}/>
    )
};

export default AllowlistBounceTable;