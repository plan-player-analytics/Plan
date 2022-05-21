import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {
    fetchDayByDayGraph,
    fetchHourByHourGraph,
    fetchPunchCardGraph,
    fetchServerCalendarGraph
} from "../../../service/serverService";
import {ErrorViewBody} from "../../../views/ErrorView";
import PunchCard from "../../graphs/PunchCard";
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
import CardTabs from "../../CardTabs";
import {faBraille, faChartArea} from "@fortawesome/free-solid-svg-icons";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import React from "react";
import TimeByTimeGraph from "../../graphs/TimeByTimeGraph";
import ServerCalendar from "../../calendar/ServerCalendar";

const DayByDayTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchDayByDayGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return <TimeByTimeGraph data={data}/>
}

const HourByHourTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchHourByHourGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return <TimeByTimeGraph data={data}/>
}

const ServerCalendarTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchServerCalendarGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return <ServerCalendar series={data.data} firstDay={data.firstDay}/>
}

const PunchCardTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPunchCardGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return <PunchCard series={data.punchCard}/>
}

const OnlineActivityGraphsCard = () => {
    const {t} = useTranslation();
    return <Card>
        <CardTabs tabs={[
            {
                name: t('html.label.dayByDay'), icon: faChartArea, color: 'blue', href: 'day-by-day',
                element: <DayByDayTab/>
            }, {
                name: t('html.label.hourByHour'), icon: faChartArea, color: 'blue', href: 'hour-by-hour',
                element: <HourByHourTab/>
            }, {
                name: t('html.label.serverCalendar'), icon: faCalendar, color: 'teal', href: 'server-calendar',
                element: <ServerCalendarTab/>
            }, {
                name: t('html.label.punchcard30days'), icon: faBraille, color: 'black', href: 'punchcard',
                element: <PunchCardTab/>
            },
        ]}/>
    </Card>
}

export default OnlineActivityGraphsCard;