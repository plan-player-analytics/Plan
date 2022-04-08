import React from "react";

import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {
    faBookOpen,
    faChartArea,
    faChartLine,
    faCrosshairs,
    faExchangeAlt,
    faExclamationCircle,
    faPowerOff,
    faSkull,
    faTachometerAlt,
    faUser,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../components/Datapoint";
import {useTranslation} from "react-i18next";
import PlayersOnlineGraph from "../components/graphs/PlayersOnlineGraph";
import {useParams} from "react-router-dom";
import {fetchPlayersOnlineGraph, fetchServerOverview} from "../service/serverService";
import {faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import {TableRow} from "../components/table/AsNumbersTable";
import ComparisonTable from "../components/table/ComparisonTable";
import BigTrend from "../components/trend/BigTrend";
import ErrorView, {ErrorViewCard} from "./ErrorView";
import {useDataRequest} from "../hooks/dataFetchHook";


const OnlineActivityCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(
        fetchPlayersOnlineGraph,
        [identifier])

    if (!data) return <></>;
    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa className="col-blue" icon={faChartArea}/> {t('html.label.onlineActivity')}
                </h6>
            </Card.Header>
            <PlayersOnlineGraph data={data}/>
        </Card>
    )
}

const Last7DaysCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <></>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    {t('html.label.last7days')}
                </h6>
            </Card.Header>
            <Card.Body>
                <Datapoint name={t('html.label.uniquePlayers')}
                           color={'blue'} icon={faUsers}
                           value={data.unique_players} bold/>
                <Datapoint name={t('html.label.uniquePlayers') + ' ' + t('html.label.perDay')}
                           color={'blue'} icon={faUser}
                           value={data.unique_players_day} bold/>
                <Datapoint name={t('html.label.newPlayers')}
                           color={'light-green'} icon={faUsers}
                           value={data.new_players} bold/>
                <Datapoint name={t('html.label.newPlayers')}
                           color={'light-green'} icon={faUsers}
                           value={data.new_players_retention_perc}
                           valueLabel={data.new_players_retention + '/' + data.new_players} bold/>
                <hr/>
                <Datapoint name={t('html.label.averageTps')}
                           color={'orange'} icon={faTachometerAlt}
                           value={data.average_tps} bold/>
                <Datapoint name={t('html.label.lowTpsSpikes')}
                           color={'red'} icon={faExclamationCircle}
                           value={data.low_tps_spikes} bold/>
                <Datapoint name={t('html.label.downtime')}
                           color={'red'} icon={faPowerOff}
                           value={data.downtime}/>
            </Card.Body>
        </Card>
    )
}


const ServerAsNumbersCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <></>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faBookOpen}/> {t('html.label.serverAsNumberse')}
                </h6>
            </Card.Header>
            <Card.Body>
                <Datapoint name={t('html.label.currentUptime')}
                           color={'light-green'} icon={faPowerOff}
                           value={data.current_uptime}/>
                <hr/>
                <Datapoint name={t('html.label.totalPlayers')}
                           color={'black'} icon={faUsers}
                           value={data.total_players} bold/>
                <Datapoint name={t('html.label.regularPlayers')}
                           color={'lime'} icon={faUsers}
                           value={data.regular_players} bold/>
                <Datapoint name={t('html.label.playersOnline')}
                           color={'blue'} icon={faUser}
                           value={data.online_players} bold/>
                <hr/>
                <Datapoint name={t('html.label.lastPeak') + ' (' + data.last_peak_date + ')'}
                           color={'blue'} icon={faChartLine}
                           value={data.last_peak_players} valueLabel={t('html.unit.players')} bold/>
                <Datapoint name={t('html.label.bestPeak') + ' (' + data.best_peak_date + ')'}
                           color={'light-green'} icon={faChartLine}
                           value={data.best_peak_players} valueLabel={t('html.unit.players')} bold/>
                <hr/>
                <Datapoint name={t('html.label.totalPlaytime')}
                           color={'green'} icon={faClock}
                           value={data.playtime}/>
                <Datapoint name={t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')}
                           color={'green'} icon={faClock}
                           value={data.player_playtime}/>
                <Datapoint name={t('html.label.sessions')}
                           color={'teal'} icon={faCalendarCheck}
                           value={data.sessions} bold/>
                <hr/>
                <Datapoint name={t('html.label.playerKills')}
                           color={'red'} icon={faCrosshairs}
                           value={data.player_kills} bold/>
                <Datapoint name={t('html.label.mobKills')}
                           color={'green'} icon={faCrosshairs}
                           value={data.mob_kills} bold/>
                <Datapoint name={t('html.label.deaths')}
                           color={'black'} icon={faSkull}
                           value={data.deaths} bold/>
            </Card.Body>
        </Card>
    )
}

const WeekComparisonCard = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <></>;
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faExchangeAlt}/> {t('html.label.weekComparison')}
                </h6>
            </Card.Header>
            <ComparisonTable comparisonHeader={t('html.label.comparing7days')}
                             headers={[data.start + ' - ' + data.midpoint, data.midpoint + ' - ' + data.end, t('html.label.trend')]}>
                <TableRow icon={faUsers} color="blue" text={t('html.label.uniquePlayers')}
                          values={[data.unique_before, data.unique_after, <BigTrend trend={data.unique_trend}/>]}/>
                <TableRow icon={faUsers} color="light-green" text={t('html.label.newPlayers')}
                          values={[data.new_before, data.new_after, <BigTrend trend={data.new_trend}/>]}/>
                <TableRow icon={faUsers} color="lime" text={t('html.label.regularPlayers')}
                          values={[data.regular_before, data.regular_after, <BigTrend trend={data.regular_trend}/>]}/>
                <TableRow icon={faClock} color="green"
                          text={t('html.label.averagePlaytime') + ' ' + t('html.label.perPlayer')}
                          values={[data.average_playtime_before, data.average_playtime_after,
                              <BigTrend trend={data.average_playtime_trend}/>]}/>
                <TableRow icon={faCalendarCheck} color="teal" text={t('html.label.sessions')}
                          values={[data.sessions_before, data.sessions_after,
                              <BigTrend trend={data.sessions_trend}/>]}/>
                <TableRow icon={faCrosshairs} color="red" text={t('html.label.playerKills')}
                          values={[data.player_kills_before, data.player_kills_after,
                              <BigTrend trend={data.player_kills_trend}/>]}/>
                <TableRow icon={faCrosshairs} color="green" text={t('html.label.mobKills')}
                          values={[data.mob_kills_before, data.mob_kills_after,
                              <BigTrend trend={data.mob_kills_trend}/>]}/>
                <TableRow icon={faSkull} color="black" text={t('html.label.deaths')}
                          values={[data.deaths_before, data.deaths_after, <BigTrend trend={data.deaths_trend}/>]}/>
            </ComparisonTable>
        </Card>
    )
}

const ServerOverview = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(
        fetchServerOverview,
        [identifier])

    if (loadingError) {
        return <ErrorView error={loadingError}/>
    }

    return (
        <section className="server_overview">
            <Row>
                <Col lg={9}>
                    <OnlineActivityCard/>
                </Col>
                <Col lg={3}>
                    <Last7DaysCard data={data ? data.last_7_days : undefined}/>
                </Col>
            </Row>
            <Row>
                <Col lg={4}>
                    <ServerAsNumbersCard data={data ? data.numbers : undefined}/>
                </Col>
                <Col lg={8}>
                    <WeekComparisonCard data={data ? data.weeks : undefined}/>
                </Col>
            </Row>
        </section>
    )
}

export default ServerOverview;