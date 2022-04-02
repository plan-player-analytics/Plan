import {useParams} from "react-router-dom";
import React from "react";
import {fetchPunchCardGraph} from "../service/serverService";
import {Card, Col, Row} from "react-bootstrap-v5";
import CardTabs from "../components/CardTabs";
import {useTranslation} from "react-i18next";
import {faBraille, faChartArea} from "@fortawesome/free-solid-svg-icons";
import PunchCard from "../components/graphs/PunchCard";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import {useDataRequest} from "../hooks/dataFetchHook";
import {ErrorViewBody} from "./ErrorView";

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

    const {data, loadingError} = useDataRequest(fetchPunchCardGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return <PunchCard series={data.punchCard}/>
}

const GraphsTabbedCard = () => {
    const {t} = useTranslation();
    return <Card><CardTabs
        tabs={[
            {
                name: t('html.label.dayByDay'), icon: faChartArea, color: 'blue', href: 'day-by-day',
                element: <DayByDayGraph/>
            }, {
                name: t('html.label.hourByHour'), icon: faChartArea, color: 'blue', href: 'hour-by-hour',
                element: <HourByHourGraph/>
            }, {
                name: t('html.label.serverCalendar'), icon: faCalendar, color: 'teal', href: 'server-calendar',
                element: <ServerCalendar/>
            }, {
                name: t('html.label.punchcard30days'), icon: faBraille, color: 'black', href: 'punchcard',
                element: <ServerPunchCard/>
            },
        ]}
    /></Card>
}

const ServerOnlineActivity = () => {
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