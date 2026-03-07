import React, {useCallback, useState} from 'react';
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import CardTabs from "../../../CardTabs";
import {faChartArea} from "@fortawesome/free-solid-svg-icons";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {
    fetchDayByDayGraph,
    fetchHourByHourGraph,
    fetchNetworkCalendarGraph,
    fetchPlayersOnlineGraph
} from "../../../../service/serverService";
import {ErrorViewBody} from "../../../../views/ErrorView.tsx";
import {ChartLoader} from "../../../navigation/Loader";
import TimeByTimeGraph from "../../../graphs/TimeByTimeGraph";
import PlayersOnlineGraph from "../../../graphs/PlayersOnlineGraph";
import {useMetadata} from "../../../../hooks/metadataHook";
import StackedPlayersOnlineGraph from "../../../graphs/StackedPlayersOnlineGraph";
import {useAuth} from "../../../../hooks/authenticationHook.tsx";
import {faCalendar} from "@fortawesome/free-regular-svg-icons";
import ServerCalendar from "../../../calendar/ServerCalendar";
import {postQuery} from "../../../../service/queryService";
import Highcharts from "highcharts/highstock";
import "highcharts/modules/no-data-to-display"
import "highcharts/modules/accessibility";
import QueryPlayerListModal from "../../../modal/QueryPlayerListModal";

const SingleProxyPlayersOnlineGraph = ({serverUUID}) => {
    const {data, loadingError} = useDataRequest(fetchPlayersOnlineGraph, [serverUUID]);

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!serverUUID || !data) return <ChartLoader/>;

    return <PlayersOnlineGraph data={data} showPlayersOnline/>
}

const MultiProxyPlayersOnlineGraph = () => {
    const {data, loadingError} = useDataRequest(fetchPlayersOnlineGraph, []);

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <StackedPlayersOnlineGraph data={data}/>
}

const PlayersOnlineTab = () => {
    const {serverUUID, networkMetadata} = useMetadata();

    if (!networkMetadata) return <ChartLoader/>

    if (networkMetadata.usingRedisBungee || networkMetadata.servers.filter(server => server.proxy).length === 1) {
        return <SingleProxyPlayersOnlineGraph serverUUID={serverUUID}/>
    } else {
        return <MultiProxyPlayersOnlineGraph/>
    }
}

const DayByDayTab = () => {
    const {data, loadingError} = useDataRequest(fetchDayByDayGraph, [])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <TimeByTimeGraph id={"day-by-day-graph"} data={data}/>
}

const HourByHourTab = () => {
    const {data, loadingError} = useDataRequest(fetchHourByHourGraph, [])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <TimeByTimeGraph id={"hour-by-hour-graph"} data={data}/>
}

const NetworkCalendarTab = () => {
    const {data, loadingError} = useDataRequest(fetchNetworkCalendarGraph, []);
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
                servers: []
            }
        }
        setQueryData(undefined);
        setModalOpen(true);
        const data = await postQuery(query);
        const loaded = data?.data;
        if (loaded) {
            setQueryData(loaded);
        }
    }, [setQueryData, setModalOpen]);

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader/>;

    return <>
        <ServerCalendar series={data.data} firstDay={data.firstDay} onSelect={onSelect}/>
        <QueryPlayerListModal open={modalOpen} toggle={closeModal} queryData={queryData}/>
    </>
}

const NetworkOnlineActivityGraphsCard = () => {
    const {hasPermission} = useAuth();
    const {t} = useTranslation();
    const tabs = [
        {
            name: t('html.label.networkOnlineActivity'),
            icon: faChartArea,
            color: 'players-online',
            href: 'online-activity',
            element: <PlayersOnlineTab/>,
            permission: 'page.network.overview.graphs.online'
        }, {
            name: t('html.label.dayByDay'), icon: faChartArea, color: 'players-unique', href: 'day-by-day',
            element: <DayByDayTab/>,
            permission: 'page.network.overview.graphs.day.by.day'
        }, {
            name: t('html.label.hourByHour'), icon: faChartArea, color: 'players-unique', href: 'hour-by-hour',
            element: <HourByHourTab/>,
            permission: 'page.network.overview.graphs.hour.by.hour'
        }, {
            name: t('html.label.networkCalendar'), icon: faCalendar, color: 'sessions', href: 'network-calendar',
            element: <NetworkCalendarTab/>,
            permission: 'page.network.overview.graphs.calendar'
        }
    ].filter(tab => hasPermission(tab.permission));
    return <Card>
        <CardTabs tabs={tabs}/>
    </Card>
};

export default NetworkOnlineActivityGraphsCard