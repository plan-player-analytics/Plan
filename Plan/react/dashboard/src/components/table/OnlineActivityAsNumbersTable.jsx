import {useTranslation} from "react-i18next";
import {faUser, faUserCircle, faUserPlus, faUsers} from "@fortawesome/free-solid-svg-icons";
import React, {useCallback} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendarCheck, faClock, faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import {useNavigation} from "../../hooks/navigationHook.tsx";
import {MS_24H, MS_MONTH, MS_WEEK} from "../../util/format/useDateFormatter.js";
import {DatapointType} from "../../dataHooks/model/datapoint/Datapoint.ts";
import {QueryDatapointTable} from "./QueryDatapointTable.tsx";
import {useParams} from "react-router";

const OnlineActivityAsNumbersTable = () => {
    const {t} = useTranslation();
    const {setHelpModalTopic} = useNavigation();
    const {identifier} = useParams();
    const openHelp = useCallback(() => setHelpModalTopic('new-player-retention'), [setHelpModalTopic]);

    return (
        <QueryDatapointTable
            filter={{server: identifier}}
            columns={[{
                key: '30d',
                filter: {afterMillisAgo: MS_MONTH}
            }, {
                key: '7d',
                filter: {afterMillisAgo: MS_WEEK}
            }, {
                key: '1d',
                filter: {afterMillisAgo: MS_24H}
            }]}
            rows={[{
                dataType: DatapointType.UNIQUE_PLAYERS_COUNT,
                color: "players-unique",
                icon: faUsers,
                text: t('html.label.uniquePlayers')
            }, {
                dataType: DatapointType.UNIQUE_PLAYERS_AVERAGE,
                color: "players-unique",
                icon: faUser,
                text: t('html.label.uniquePlayers') + ' ' + t('html.label.perDay')
            }, {
                dataType: DatapointType.NEW_PLAYERS,
                color: "players-new",
                icon: faUsers,
                text: t('html.label.newPlayers')
            }, {
                dataType: DatapointType.NEW_PLAYERS_AVERAGE,
                color: "players-new",
                icon: faUserPlus,
                text: t('html.label.newPlayers') + ' ' + t('html.label.perDay')
            }, {
                dataType: DatapointType.NEW_PLAYER_RETENTION,
                color: "players-new",
                icon: faUserCircle,
                text: <>{t('html.label.newPlayerRetention')} <span>
                                <button onClick={openHelp}><Fa className={"col-text"}
                                                               icon={faQuestionCircle}/>
                                </button></span></>
            }, {
                dataType: DatapointType.PLAYTIME,
                color: "playtime",
                icon: faClock,
                text: t('html.label.playtime')
            }, {
                dataType: DatapointType.PLAYTIME_PER_DAY_AVERAGE,
                color: "playtime",
                icon: faClock,
                text: t('html.label.averagePlaytime') + ' ' + t('html.label.perDay')
            }, {
                dataType: DatapointType.SESSION_LENGTH_AVERAGE,
                color: "sessions",
                icon: faClock,
                text: t('html.label.averageSessionLength')
            }, {
                dataType: DatapointType.SESSION_COUNT,
                color: "sessions",
                icon: faCalendarCheck,
                text: t('html.label.sessions')
            }]}/>
    )
}

export default OnlineActivityAsNumbersTable;