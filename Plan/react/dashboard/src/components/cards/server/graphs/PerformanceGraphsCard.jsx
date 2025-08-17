import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchOptimizedPerformance, fetchPingGraph, fetchPluginHistory} from "../../../../service/serverService";
import {ErrorViewBody} from "../../../../views/ErrorView";
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap";
import CardTabs from "../../../CardTabs";
import {faGears, faHdd, faMap, faMicrochip, faSignal, faTachometerAlt} from "@fortawesome/free-solid-svg-icons";
import React, {useEffect, useState} from "react";
import {ChartLoader} from "../../../navigation/Loader";
import AllPerformanceGraph from "../../../graphs/performance/AllPerformanceGraph";
import TpsPerformanceGraph from "../../../graphs/performance/TpsPerformanceGraph";
import CpuRamPerformanceGraph from "../../../graphs/performance/CpuRamPerformanceGraph";
import WorldPerformanceGraph from "../../../graphs/performance/WorldPerformanceGraph";
import DiskPerformanceGraph from "../../../graphs/performance/DiskPerformanceGraph";
import PingGraph from "../../../graphs/performance/PingGraph";
import {mapPerformanceDataToSeries} from "../../../../util/graphs";
import {useAuth} from "../../../../hooks/authenticationHook";
import {GraphExtremesContextProvider} from "../../../../hooks/interaction/graphExtremesContextHook.jsx";

const AllGraphTab = ({data, dataSeries, pluginHistorySeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <AllPerformanceGraph id="server-performance-all-chart" data={data} dataSeries={dataSeries}
                                pluginHistorySeries={pluginHistorySeries}/>
}

const TpsGraphTab = ({data, dataSeries, pluginHistorySeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <TpsPerformanceGraph id="server-performance-tps-chart" data={data} dataSeries={dataSeries}
                                pluginHistorySeries={pluginHistorySeries}/>
}

const CpuRamGraphTab = ({data, dataSeries, pluginHistorySeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <CpuRamPerformanceGraph id="server-performance-cpuram-chart" data={data} dataSeries={dataSeries}
                                   pluginHistorySeries={pluginHistorySeries}/>
}

const WorldGraphTab = ({data, dataSeries, pluginHistorySeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <WorldPerformanceGraph id="server-performance-world-chart" data={data} dataSeries={dataSeries}
                                  pluginHistorySeries={pluginHistorySeries}/>
}

const DiskGraphTab = ({data, dataSeries, pluginHistorySeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <DiskPerformanceGraph id="server-performance-disk-chart" data={data} dataSeries={dataSeries}
                                 pluginHistorySeries={pluginHistorySeries}/>
}

const PingGraphTab = ({identifier}) => {
    const {data, loadingError} = useDataRequest(fetchPingGraph, [identifier]);
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader style={{height: "450px"}}/>;

    return <PingGraph id="server-performance-ping-chart" data={data}/>;
}

const PerformanceGraphsCard = () => {
    const {t} = useTranslation();
    const {authRequired, hasPermission, hasChildPermission} = useAuth();

    const {identifier} = useParams();
    const {data, loadingError} = useDataRequest(fetchOptimizedPerformance, [identifier]);
    const [parsedData, setParsedData] = useState(undefined);
    const {
        data: pluginHistory,
        loadingError: pluginHistoryLoadingError
    } = useDataRequest(fetchPluginHistory, [identifier], authRequired && hasPermission('page.server.plugin.history'));
    const [pluginHistorySeries, setPluginHistorySeries] = useState(undefined);

    useEffect(() => {
        if (data) {
            mapPerformanceDataToSeries(data.values).then(parsed => setParsedData(parsed))
        }
    }, [data, setParsedData]);
    useEffect(() => {
        // https://stackoverflow.com/a/34890276/20825073
        const groupBy = function (xs, key) {
            return xs.reduce(function (rv, x) {
                (rv[x[key]] = rv[x[key]] || []).push(x);
                return rv;
            }, {});
        };

        if (pluginHistory) {
            const grouped = groupBy(pluginHistory.history.reverse(), 'modified');
            setPluginHistorySeries({
                type: 'flags',
                accessibility: {
                    exposeAsGroupOnly: true,
                    description: t('html.label.pluginVersionHistory')
                },
                name: t('html.label.pluginHistory'),
                tooltip: {headerFormat: ''},
                data: Object.entries(grouped).map(entry => {
                    const installedLines = entry[1].filter(p => p.version).map(plugin => plugin.name + ': ' + plugin.version).join(', <br>');
                    const uninstalledLines = entry[1].filter(p => !p.version).map(plugin => plugin.name).join(', <br>');
                    return {
                        x: entry[0],
                        title: entry[1].length,
                        text: (installedLines.length ? '<b>' + t('html.label.installed') + '</b><br>' + installedLines : '') +
                            (uninstalledLines.length ? '<b>' + t('html.label.uninstalled') + '</b><br>' + uninstalledLines : '')
                    }
                })
            })
        }
    }, [pluginHistory, setPluginHistorySeries, t]);

    const tabs = [
        {
            name: t('html.label.all'), icon: faGears, color: 'chunks', href: 'all',
            element: <AllGraphTab data={data} dataSeries={parsedData} pluginHistorySeries={pluginHistorySeries}
                                  loadingError={loadingError || pluginHistoryLoadingError}/>,
            permission: 'page.server.performance.graphs'
        }, {
            name: t('html.label.tps'), icon: faTachometerAlt, color: 'tps', href: 'tps',
            element: <TpsGraphTab data={data} dataSeries={parsedData} pluginHistorySeries={pluginHistorySeries}
                                  loadingError={loadingError || pluginHistoryLoadingError}/>,
            permission: 'page.server.performance.graphs.tps'
        }, {
            name: t('html.label.cpuRam'), icon: faMicrochip, color: 'ram', href: 'cpu-ram',
            element: <CpuRamGraphTab data={data} dataSeries={parsedData} pluginHistorySeries={pluginHistorySeries}
                                     loadingError={loadingError || pluginHistoryLoadingError}/>,
            permission: ['page.server.performance.graphs.cpu', 'page.server.performance.graphs.ram']
        }, {
            name: t('html.label.world'), icon: faMap, color: 'entities', href: 'world-load',
            element: <WorldGraphTab data={data} dataSeries={parsedData} pluginHistorySeries={pluginHistorySeries}
                                    loadingError={loadingError || pluginHistoryLoadingError}/>,
            permission: ['page.server.performance.graphs.entities', 'page.server.performance.graphs.chunks']
        }, {
            name: t('html.label.ping'), icon: faSignal, color: 'ping', href: 'ping',
            element: <PingGraphTab identifier={identifier}/>,
            permission: 'page.server.performance.graphs.ping'
        }, {
            name: t('html.label.diskSpace'), icon: faHdd, color: 'disk', href: 'disk',
            element: <DiskGraphTab data={data} dataSeries={parsedData} pluginHistorySeries={pluginHistorySeries}
                                   loadingError={loadingError || pluginHistoryLoadingError}/>,
            permission: 'page.server.performance.graphs.disk'
        },
    ].filter(tab => hasChildPermission(tab.permission));
    return <Card id={"performance-graphs"}>
        <GraphExtremesContextProvider>
            <CardTabs tabs={tabs}/>
        </GraphExtremesContextProvider>
    </Card>
}

export default PerformanceGraphsCard;