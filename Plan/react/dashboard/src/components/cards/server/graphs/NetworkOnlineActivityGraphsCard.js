import React from 'react';
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import CardTabs from "../../../CardTabs";
import {faChartArea} from "@fortawesome/free-solid-svg-icons";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchDayByDayGraph, fetchHourByHourGraph, fetchPlayersOnlineGraph} from "../../../../service/serverService";
import {ErrorViewBody} from "../../../../views/ErrorView";
import {ChartLoader} from "../../../navigation/Loader";
import TimeByTimeGraph from "../../../graphs/TimeByTimeGraph";
import PlayersOnlineGraph from "../../../graphs/PlayersOnlineGraph";
import {useMetadata} from "../../../../hooks/metadataHook";
import StackedPlayersOnlineGraph from "../../../graphs/StackedPlayersOnlineGraph";
import {useAuth} from "../../../../hooks/authenticationHook";

const SingleProxyPlayersOnlineGraph = ({serverUUID}) => {
    const {data, loadingError} = useDataRequest(fetchPlayersOnlineGraph, [serverUUID]);

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!serverUUID || !data) return <ChartLoader/>;

    return <PlayersOnlineGraph data={data}/>
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

const NetworkOnlineActivityGraphsCard = () => {
    const {hasPermission} = useAuth();
    const {t} = useTranslation();
    const tabs = [
        {
            name: t('html.label.networkOnlineActivity'), icon: faChartArea, color: 'blue', href: 'online-activity',
            element: <PlayersOnlineTab/>,
            permission: 'page.network.overview.graphs.online'
        }, {
            name: t('html.label.dayByDay'), icon: faChartArea, color: 'blue', href: 'day-by-day',
            element: <DayByDayTab/>,
            permission: 'page.network.overview.graphs.day.by.day'
        }, {
            name: t('html.label.hourByHour'), icon: faChartArea, color: 'blue', href: 'hour-by-hour',
            element: <HourByHourTab/>,
            permission: 'page.network.overview.graphs.hour.by.hour'
        }
    ].filter(tab => hasPermission(tab.permission));
    return <Card>
        <CardTabs tabs={tabs}/>
    </Card>
};

export default NetworkOnlineActivityGraphsCard