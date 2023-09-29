import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {
    fetchDayByDayGraph,
    fetchHourByHourGraph,
    fetchPunchCardGraph,
    fetchServerCalendarGraph
} from "../../../../service/serverService";
import {ErrorViewBody} from "../../../../views/ErrorView";
import PunchCard from "../../../graphs/PunchCard";
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import CardTabs from "../../../CardTabs";
import {faBraille, faChartArea} from "@fortawesome/free-solid-svg-icons";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import React from "react";
import TimeByTimeGraph from "../../../graphs/TimeByTimeGraph";
import ServerCalendar from "../../../calendar/ServerCalendar";
import {ChartLoader} from "../../../navigation/Loader";
import {useAuth} from "../../../../hooks/authenticationHook";

const DayByDayTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchDayByDayGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <TimeByTimeGraph id={"day-by-day-graph"} data={data}/>
}

const HourByHourTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchHourByHourGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <TimeByTimeGraph id={"hour-by-hour-graph"}data={data}/>
}

const ServerCalendarTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchServerCalendarGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <ServerCalendar series={data.data} firstDay={data.firstDay}/>
}

const PunchCardTab = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPunchCardGraph, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <PunchCard series={data.punchCard}/>
}

const OnlineActivityGraphsCard = () => {
    const {hasPermission} = useAuth();
    const {t} = useTranslation();
    const tabs = [
        {
            name: t('html.label.dayByDay'), icon: faChartArea, color: 'blue', href: 'day-by-day',
            element: <DayByDayTab/>,
            permission: 'page.server.online.activity.graphs.day.by.day'
        }, {
            name: t('html.label.hourByHour'), icon: faChartArea, color: 'blue', href: 'hour-by-hour',
            element: <HourByHourTab/>,
            permission: 'page.server.online.activity.graphs.hour.by.hour'
        }, {
            name: t('html.label.serverCalendar'), icon: faCalendar, color: 'teal', href: 'server-calendar',
            element: <ServerCalendarTab/>,
            permission: 'page.server.online.activity.graphs.calendar'
        }, {
            name: t('html.label.punchcard30days'), icon: faBraille, color: 'black', href: 'punchcard',
            element: <PunchCardTab/>,
            permission: 'page.server.online.activity.graphs.punchcard'
        },
    ].filter(tab => hasPermission(tab.permission));
    return <Card id={"online-activity-graphs"}>
        <CardTabs tabs={tabs}/>
    </Card>
}

export default OnlineActivityGraphsCard;