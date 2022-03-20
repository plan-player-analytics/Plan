import {useParams} from "react-router-dom";
import React, {useCallback, useEffect, useState} from "react";
import {fetchPunchCardGraph, fetchServerOverview} from "../service/serverService";
import {Card, Col, Row} from "react-bootstrap-v5";
import CardTabs from "../components/CardTabs";
import {useTranslation} from "react-i18next";
import {faBraille, faChartArea} from "@fortawesome/free-solid-svg-icons";
import PunchCard from "../components/graphs/PunchCard";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";

const DayByDayGraph = () => {
    return <></>
}

const HourByHourGraph = () => {
    return <></>
}

const ServerCalendar = () => {
    return <></>
}

const ServerPunchCard = () => {
    const {identifier} = useParams();
    const [data, setData] = useState(undefined);

    const loadData = useCallback(async () => setData(await fetchPunchCardGraph(identifier)), [identifier]);

    useEffect(() => {
        loadData();
    }, [loadData])

    if (!data) return <></>;

    return <PunchCard series={data.punchCard}/>
}

const GraphsTabbedCard = () => {
    const {t} = useTranslation();
    return <Card><CardTabs
        tabs={[
            {
                name: t('html.title.graph.dayByDay'), icon: faChartArea, color: 'blue', href: 'day-by-day',
                element: <DayByDayGraph/>
            }, {
                name: t('html.title.graph.hourByHour'), icon: faChartArea, color: 'blue', href: 'hour-by-hour',
                element: <HourByHourGraph/>
            }, {
                name: t('html.title.serverCalendar'), icon: faCalendar, color: 'teal', href: 'server-calendar',
                element: <ServerCalendar/>
            }, {
                name: t('html.title.punchcard30days'), icon: faBraille, color: 'black', href: 'punchcard',
                element: <ServerPunchCard/>
            },
        ]}
    /></Card>
}

const ServerOnlineActivity = () => {
    const {identifier} = useParams();
    const [data, setData] = useState(undefined);

    const loadData = useCallback(async () => setData(await fetchServerOverview(identifier)), [identifier]);

    useEffect(() => {
        loadData();
    }, [loadData])

    return (
        <section className="server_online_activity_overview">
            <Row>
                <Col lg={12}>
                    <GraphsTabbedCard/>
                </Col>
            </Row>
            <Row>
                <Col lg={4}>
                </Col>
                <Col lg={8}>
                </Col>
            </Row>
        </section>
    )
}

export default ServerOnlineActivity