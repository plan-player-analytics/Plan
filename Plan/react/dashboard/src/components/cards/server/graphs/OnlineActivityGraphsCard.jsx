import {useParams} from "react-router";
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
import React, {useCallback, useState} from "react";
import TimeByTimeGraph from "../../../graphs/TimeByTimeGraph";
import ServerCalendar from "../../../calendar/ServerCalendar";
import {ChartLoader} from "../../../navigation/Loader";
import {useAuth} from "../../../../hooks/authenticationHook";
import Highcharts from "highcharts/highstock";
import "highcharts/modules/no-data-to-display"
import "highcharts/modules/accessibility";
import {postQuery} from "../../../../service/queryService";
import QueryPlayerListModal from "../../../modal/QueryPlayerListModal";
import {useMetadata} from "../../../../hooks/metadataHook";

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

    return <TimeByTimeGraph id={"hour-by-hour-graph"} data={data}/>
}

const ServerCalendarTab = () => {
    const {identifier} = useParams();
    const {data, loadingError} = useDataRequest(fetchServerCalendarGraph, [identifier]);
    const {networkMetadata} = useMetadata();

    const [modalOpen, setModalOpen] = useState(false);
    const [queryData, setQueryData] = useState(undefined);

    const closeModal = useCallback(() => {
        setModalOpen(false);
    }, [setModalOpen]);

    const onSelect = useCallback(async selectionInfo => {
        const start = Highcharts.dateFormat('%d/%m/%Y', selectionInfo.start);
        const end = Highcharts.dateFormat('%d/%m/%Y', selectionInfo.end);
        const query = {
            filters: [{
                kind: "playedBetween",
                parameters: {
                    afterDate: start, afterTime: "00:00",
                    beforeDate: end, beforeTime: "00:00"
                }
            }],
            view: {
                afterDate: start, afterTime: "00:00",
                beforeDate: end, beforeTime: "00:00",
                servers: networkMetadata?.servers.filter(server => server.serverUUID === identifier) || []
            }
        }
        setQueryData(undefined);
        setModalOpen(true);
        const data = await postQuery(query);
        const loaded = data?.data;
        if (loaded) {
            setQueryData(loaded);
        }
    }, [setQueryData, setModalOpen, networkMetadata, identifier]);

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <>
        <ServerCalendar series={data.data} firstDay={data.firstDay} onSelect={onSelect}/>
        <QueryPlayerListModal open={modalOpen} toggle={closeModal} queryData={queryData}/>
    </>
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
            name: t('html.label.dayByDay'), icon: faChartArea, color: 'players-unique', href: 'day-by-day',
            element: <DayByDayTab/>,
            permission: 'page.server.online.activity.graphs.day.by.day'
        }, {
            name: t('html.label.hourByHour'), icon: faChartArea, color: 'players-unique', href: 'hour-by-hour',
            element: <HourByHourTab/>,
            permission: 'page.server.online.activity.graphs.hour.by.hour'
        }, {
            name: t('html.label.serverCalendar'), icon: faCalendar, color: 'sessions', href: 'server-calendar',
            element: <ServerCalendarTab/>,
            permission: 'page.server.online.activity.graphs.calendar'
        }, {
            name: t('html.label.punchcard30days'), icon: faBraille, color: 'text', href: 'punchcard',
            element: <PunchCardTab/>,
            permission: 'page.server.online.activity.graphs.punchcard'
        },
    ].filter(tab => hasPermission(tab.permission));
    return <Card id={"online-activity-graphs"}>
        <CardTabs tabs={tabs}/>
    </Card>
}

export default OnlineActivityGraphsCard;