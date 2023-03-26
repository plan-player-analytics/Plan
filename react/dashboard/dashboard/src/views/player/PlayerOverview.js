import React from "react";
import {Card, Col} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import {faBookOpen, faBraille, faCrosshairs, faGlobe, faSkull, faWifi} from "@fortawesome/free-solid-svg-icons";
import Scrollable from "../../components/Scrollable";
import PunchCard from "../../components/graphs/PunchCard";
import AsNumbersTable from "../../components/table/AsNumbersTable";
import {useTheme} from "../../hooks/themeHook";
import {usePlayer} from "../layout/PlayerPage";
import {useTranslation} from "react-i18next";
import PlayerOverviewCard from "../../components/cards/player/PlayerOverviewCard";
import NicknamesCard from "../../components/cards/player/NicknamesCard";
import {TableRow} from "../../components/table/TableRow";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const ConnectionsCard = ({player}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faWifi}/> {t('html.label.connectionInfo')}
                </h6>
            </Card.Header>
            <Scrollable>
                <table className={"table table-striped mb-0" + (nightModeEnabled ? " table-dark" : '')}>
                    <thead className="bg-green">
                    <tr>
                        <th><Fa icon={faGlobe}/> {t('html.label.country')}</th>
                        <th><Fa icon={faClock}/> {t('html.label.lastConnected')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {player.connections.map((connection, i) => (<tr key={'connection-' + i}>
                        <td>{connection.geolocation}</td>
                        <td>{connection.date}</td>
                    </tr>))}
                    </tbody>
                </table>
            </Scrollable>
        </Card>
    )
}

const PunchCardCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faBraille}/> {t('html.label.punchcard')}
                </h6>
            </Card.Header>
            <PunchCard series={player.punchcard_series}/>
        </Card>
    )
}

const OnlineActivityCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faBookOpen}/> {t('html.label.onlineActivity')}
                </h6>
            </Card.Header>
            <AsNumbersTable headers={[t('html.label.last30days'), t('html.label.last7days')]}>
                <TableRow icon={faClock} color="green" text={t('html.label.playtime')}
                          values={[player.online_activity.playtime_30d, player.online_activity.playtime_7d]}/>
                <TableRow icon={faClock} color="green" text={t('html.label.activePlaytime')}
                          values={[player.online_activity.active_playtime_30d, player.online_activity.active_playtime_7d]}/>
                <TableRow icon={faClock} color="gray" text={t('html.label.afk')}
                          values={[player.online_activity.afk_time_30d, player.online_activity.afk_time_7d]}/>
                <TableRow icon={faClock} color="teal" text={t('html.label.medianSessionLength')}
                          values={[player.online_activity.median_session_length_30d, player.online_activity.median_session_length_7d]}/>
                <TableRow icon={faCalendarCheck} color="teal" text={t('html.label.sessions')}
                          values={[player.online_activity.session_count_30d, player.online_activity.session_count_7d]}/>
                <TableRow icon={faCrosshairs} color="red" text={t('html.label.playerKills')}
                          values={[player.online_activity.player_kill_count_30d, player.online_activity.player_kill_count_7d]}/>
                <TableRow icon={faCrosshairs} color="green" text={t('html.label.mobKills')}
                          values={[player.online_activity.mob_kill_count_30d, player.online_activity.mob_kill_count_7d]}/>
                <TableRow icon={faSkull} color="black" text={t('html.label.deaths')}
                          values={[player.online_activity.death_count_30d, player.online_activity.death_count_7d]}/>
            </AsNumbersTable>
        </Card>
    )
}

const PlayerOverview = () => {
    const {player} = usePlayer();

    return (
        <LoadIn>
            <section className="player-overview">
                <ExtendableRow id={'row-player-overview-0'}>
                    <Col lg={6}>
                        <PlayerOverviewCard player={player}/>
                        <NicknamesCard player={player}/>
                        <ConnectionsCard player={player}/>
                    </Col>
                    <Col lg={6}>
                        <PunchCardCard player={player}/>
                        <OnlineActivityCard player={player}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
}

export default PlayerOverview;