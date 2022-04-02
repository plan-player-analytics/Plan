import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faAddressBook, faCalendar, faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import {
    faBookOpen,
    faBraille,
    faCircle,
    faCrosshairs,
    faGavel,
    faGlobe,
    faLocationArrow,
    faServer,
    faSignal,
    faSignature,
    faSkull,
    faUser,
    faUserPlus,
    faWifi
} from "@fortawesome/free-solid-svg-icons";
import {faSuperpowers} from "@fortawesome/free-brands-svg-icons";
import Scrollable from "../components/Scrollable";
import PunchCard from "../components/graphs/PunchCard";
import Datapoint from "../components/Datapoint";
import AsNumbersTable, {TableRow} from "../components/table/AsNumbersTable";
import {useTheme} from "../hooks/themeHook";
import {usePlayer} from "./PlayerPage";
import {useMetadata} from "../hooks/metadataHook";
import {useTranslation} from "react-i18next";

const PlayerOverviewCard = ({player}) => {
    const {t} = useTranslation();
    const {getPlayerHeadImageUrl} = useMetadata();
    const headImageUrl = getPlayerHeadImageUrl(player.info.name, player.info.uuid)

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faAddressBook}/> {player.info.name}
                </h6>
            </Card.Header>
            <Card.Body>
                <Row>
                    <Col sm={4}>
                        <p>
                            <Fa icon={faCircle} className={player.info.online ? "col-green" : "col-red"}/>
                            {' ' + (player.info.online ? t('html.value.online') : t('html.value.offline'))}
                        </p>
                        {player.info.operator ?
                            <p><Fa icon={faSuperpowers} className="col-blue"/> {t('html.label.operator')}</p> : ''}
                        <p><Fa icon={faGavel}
                               className="col-brown"/> {t('html.label.timesKicked')}: {player.info.kick_count}</p>
                    </Col>
                    <Col sm={4}>
                        <img className="rounded mx-auto d-block"
                             alt="player head"
                             src={headImageUrl}/>
                    </Col>
                    <Col sm={4}>
                        <p><Fa icon={faCrosshairs}
                               className="col-red"/> {t('html.label.playerKills')}: {player.info.player_kill_count}
                        </p>
                        <p><Fa icon={faCrosshairs}
                               className="col-green"/> {t('html.label.mobKills')}: {player.info.mob_kill_count}</p>
                        <p><Fa icon={faSkull}/> {t('html.label.deaths')}: {player.info.death_count}</p>
                    </Col>
                </Row>
                <hr/>
                <Row>
                    <Col lg={6}>
                        <Datapoint
                            icon={faClock} color="green"
                            name={t('html.label.totalPlaytime')} value={player.info.playtime}
                        />
                        <Datapoint
                            icon={faClock} color="green"
                            name={t('html.label.totalActive')} value={player.info.active_playtime}
                        />
                        <Datapoint
                            icon={faClock} color="grey"
                            name={t('html.label.totalAfk')} value={player.info.afk_time}
                        />
                        <hr/>
                        <Datapoint
                            icon={faCalendarCheck} color="teal"
                            name={t('html.label.sessions')} value={player.info.session_count} bold
                        />
                        <Datapoint
                            icon={faClock} color="teal"
                            name={t('html.label.longestSession')} value={player.info.longest_session_length}
                        />
                        <Datapoint
                            icon={faClock} color="teal"
                            name={t('html.label.sessionMedian')} value={player.info.session_median}
                        />
                        <hr/>
                        <Datapoint
                            icon={faUserPlus} color="light-green"
                            name={t('html.label.registered')} value={player.info.registered} boldTitle
                        />
                    </Col>
                    <Col lg={6}>
                        <Datapoint
                            icon={faUser} color="amber"
                            name={t('html.label.activityIndex')}
                            value={player.info.activity_index} bold
                            valueLabel={player.info.activity_index_group}
                        />
                        <Datapoint
                            icon={faServer} color="light-green"
                            name={t('html.label.favoriteServer')} value={player.info.favorite_server}
                        />
                        <Datapoint
                            icon={faLocationArrow} color="amber"
                            name={t('html.label.joinAddress')} value={player.info.latest_join_address}
                        />
                        <hr/>
                        <Datapoint
                            icon={faSignal} color="amber"
                            name={t('html.label.averagePing')} value={player.info.average_ping}
                        />
                        <Datapoint
                            icon={faSignal} color="amber"
                            name={t('html.label.bestPing')} value={player.info.best_ping}
                        />
                        <Datapoint
                            icon={faSignal} color="amber"
                            name={t('html.label.worstPing')} value={player.info.worst_ping}
                        />
                        <hr/>
                        <Datapoint
                            icon={faCalendar} color="teal"
                            name={t('html.label.lastSeen')} value={player.info.last_seen} boldTitle
                        />
                    </Col>
                </Row>
            </Card.Body>
        </Card>
    );
}

const NicknamesCard = ({player}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faSignature}/> {t('html.label.seenNicknames')}
                </h6>
            </Card.Header>
            <Scrollable>
                <table className={"table table-striped mb-0" + (nightModeEnabled ? " table-dark" : '')}>
                    <thead className="bg-purple">
                    <tr>
                        <th><Fa icon={faSignature}/> {t('html.label.nickname')}</th>
                        <th><Fa icon={faServer}/> {t('html.label.server')}</th>
                        <th><Fa icon={faClock}/> {t('html.label.lastSeen')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    {player.nicknames.map((nickname, i) => (<tr key={'nick-' + i}>
                        <td>{nickname.nickname}</td>
                        <td>{nickname.server}</td>
                        <td>{nickname.date}</td>
                    </tr>))}
                    </tbody>
                </table>
            </Scrollable>
        </Card>
    );
}

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
        <section className="player_overview">
            <Row>
                <Col lg={6}>
                    <PlayerOverviewCard player={player}/>
                    <NicknamesCard player={player}/>
                    <ConnectionsCard player={player}/>
                </Col>
                <Col lg={6}>
                    <PunchCardCard player={player}/>
                    <OnlineActivityCard player={player}/>
                </Col>
            </Row>
        </section>
    )
}

export default PlayerOverview;