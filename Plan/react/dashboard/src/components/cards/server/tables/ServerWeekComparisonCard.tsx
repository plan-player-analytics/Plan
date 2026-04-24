import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faExchangeAlt, faSkull, faUsers} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import React from "react";
import {QueryDatapointTable} from "../../../table/QueryDatapointTable";
import {useParams} from "react-router";
import {MS_WEEK} from "../../../../util/format/useDateFormatter";
import {DatapointType} from "../../../../dataHooks/model/datapoint/Datapoint";

const ServerWeekComparisonCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();
    return (
        <Card id={"week-comparison"}>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faExchangeAlt}/> {t('html.label.weekComparison')}
                </h6>
            </Card.Header>
            <QueryDatapointTable comparisonHeader={t('html.label.comparing7days')}
                                 filter={{server: identifier}} showTrend
                                 columns={[{
                                     key: 'previous',
                                     filter: {afterMillisAgo: MS_WEEK * 2, beforeMillisAgo: MS_WEEK}
                                 }, {
                                     key: 'next',
                                     filter: {afterMillisAgo: MS_WEEK}
                                 }]}
                                 rows={[{
                                     dataType: DatapointType.UNIQUE_PLAYERS_COUNT,
                                     color: "players-unique",
                                     icon: faUsers,
                                     text: t('html.label.uniquePlayers')
                                 }, {
                                     dataType: DatapointType.NEW_PLAYERS,
                                     color: "players-new",
                                     icon: faUsers,
                                     text: t('html.label.newPlayers')
                                 }, {
                                     dataType: DatapointType.REGULAR_PLAYERS,
                                     color: "players-regular",
                                     icon: faUsers,
                                     text: t('html.label.regularPlayers')
                                 }, {
                                     dataType: DatapointType.PLAYTIME_PER_PLAYER_AVERAGE,
                                     color: "playtime",
                                     icon: faClock,
                                     text: t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')
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
                                 }, {
                                     dataType: DatapointType.PLAYER_KILLS,
                                     color: "player-kills",
                                     icon: faCrosshairs,
                                     text: t('html.label.playerKills')
                                 }, {
                                     dataType: DatapointType.MOB_KILLS,
                                     color: "mob-kills",
                                     icon: faCrosshairs,
                                     text: t('html.label.mobKills')
                                 }, {
                                     dataType: DatapointType.DEATHS,
                                     color: "deaths",
                                     icon: faSkull,
                                     text: t('html.label.deaths')
                                 }]}/>
        </Card>
    )
}

export default ServerWeekComparisonCard