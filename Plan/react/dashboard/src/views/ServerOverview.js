import React, {useCallback, useEffect, useState} from "react";

import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {
    faChartArea,
    faExclamationCircle,
    faPowerOff,
    faTachometerAlt,
    faUser,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../components/Datapoint";
import {useTranslation} from "react-i18next";
import PlayersOnlineGraph from "../components/graphs/PlayersOnlineGraph";
import {useParams} from "react-router-dom";
import {fetchPlayersOnlineGraph, fetchServerOverview} from "../service/serverService";


const OnlineActivityCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();
    const [data, setData] = useState(undefined);

    const loadData = useCallback(async () => setData(await fetchPlayersOnlineGraph(identifier)), [identifier]);

    useEffect(() => {
        loadData();
    }, [loadData])

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa className="col-blue" icon={faChartArea}/> {t('html.title.onlineActivity')}
                </h6>
            </Card.Header>
            {data ? <PlayersOnlineGraph data={data}/> : ''}
        </Card>
    )
}

const Last7DaysCard = ({data}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    {t('html.title.last7days')}
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

const ServerOverview = () => {
    const {identifier} = useParams();
    const [data, setData] = useState(undefined);

    const loadData = useCallback(async () => setData(await fetchServerOverview(identifier)), [identifier]);

    useEffect(() => {
        loadData();
    }, [loadData])

    console.log(data)

    return (
        <section className="player_overview">
            <Row>
                <Col lg={9}>
                    <OnlineActivityCard/>
                </Col>
                <Col lg={3}>
                    <Last7DaysCard data={data ? data.last_7_days : undefined}/>
                </Col>
            </Row>
        </section>
    )
}

export default ServerOverview;