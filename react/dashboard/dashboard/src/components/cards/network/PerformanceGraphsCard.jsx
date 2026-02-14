import React, {useEffect, useMemo, useState} from 'react';
import CardTabs from "../../CardTabs";
import {
    faDragon,
    faHdd,
    faMap,
    faMicrochip,
    faSignal,
    faStopwatch,
    faTachometerAlt,
    faUser
} from "@fortawesome/free-solid-svg-icons";
import {Card} from "react-bootstrap";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {fetchPingGraph} from "../../../service/serverService";
import {tooltip, yAxisConfigurations} from "../../../util/graphs";
import {useTranslation} from "react-i18next";
import {CardLoader, ChartLoader} from "../../navigation/Loader";
import LineGraph from "../../graphs/LineGraph";
import {ErrorViewBody, ErrorViewCard} from "../../../views/ErrorView";
import PingGraph from "../../graphs/performance/PingGraph";
import {useMetadata} from "../../../hooks/metadataHook";
import {getColorConverter} from "../../../util/Color.js";
import {
    GraphExtremesContextProvider,
    useGraphExtremesContext
} from "../../../hooks/interaction/graphExtremesContextHook.jsx";

const Tab = ({id, data, yAxis}) => {
    const {extremes, onSetExtremes} = useGraphExtremesContext();
    return (
        <LineGraph id={'performance-' + id} series={data} legendEnabled tall yAxis={yAxis} extremes={extremes}
                   onSetExtremes={onSetExtremes}/>
    )
}

const PingTab = ({identifier}) => {
    const {data, loadingError} = useDataRequest(fetchPingGraph, [identifier]);

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader style={{height: "450px"}}/>;

    return <PingGraph id="network-performance-ping-chart" data={data}/>;
}

const PerformanceGraphsCard = ({data}) => {
    const {t} = useTranslation();
    const {networkMetadata} = useMetadata();

    const [performanceSeries, setPerformanceSeries] = useState({
        players: [],
        tps: [],
        cpu: [],
        ram: [],
        entities: [],
        chunks: [],
        disk: [],
        msptAverage: [],
        mspt95thPercentile: []
    });

    useEffect(() => {
        if (!data?.zones) return;

        const zones = {
            tps: [{
                value: data.zones.tpsThresholdMed,
                color: "var(--color-graphs-tps-low)"
            }, {
                value: data.zones.tpsThresholdHigh,
                color: "var(--color-graphs-tps-medium)"
            }, {
                value: 30,
                color: "var(--color-graphs-tps-high)"
            }],
            disk: [{
                value: data.zones.diskThresholdMed,
                color: "var(--color-graphs-disk-low)"
            }, {
                value: data.zones.diskThresholdHigh,
                color: "var(--color-graphs-disk-medium)"
            }, {
                value: Number.MAX_VALUE,
                color: "var(--color-graphs-disk-high)"
            }]
        };

        const serverData = [];
        for (let i = 0; i < data.servers.length; i++) {
            const server = data.servers[i];
            const values = data.values[i];
            serverData.push({
                serverName: server.serverName,
                values
            });
        }

        const series = {
            players: [],
            tps: [],
            cpu: [],
            ram: [],
            entities: [],
            chunks: [],
            disk: [],
            msptAverage: [],
            mspt95thPercentile: []
        }

        const spline = 'spline';

        const changeColor = (color, index) => {
            return getColorConverter(color)
                .increaseHue(index * 5 / 360)
                .toHex();
        }

        const minuteResolution = point => {
            // Ensure that the points can be stacked by moving data to minute level
            point[0] -= (point[0] % 60000);
            return point;
        }

        serverData.forEach((server, i) => {
            const playersOnlineColor = changeColor("var(--color-graphs-players-online)", i);
            const tpsColor = changeColor("var(--color-graphs-tps-high)", i);
            const tpsZone = zones.tps;
            const cpuColor = changeColor("var(--color-graphs-cpu)", i);
            const ramColors = changeColor("var(--color-graphs-ram)", i);
            const entitiesColor = changeColor("var(--color-graphs-entities)", i);
            const chunksColor = changeColor("var(--color-graphs-chunks)", i);
            const diskColor = changeColor("var(--color-graphs-disk-high)", i);
            const msptAverageColor = changeColor("var(--color-data-performance-mspt-average)", i);
            const msptPercentileColor = changeColor("var(--color-data-performance-mspt-percentile)", i);
            const diskZones = zones.disk;

            series.players.push({
                name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
                data: server.values.playersOnline.map(minuteResolution), color: playersOnlineColor, yAxis: 0
            });
            series.tps.push({
                name: server.serverName, type: spline, tooltip: tooltip.twoDecimals,
                data: server.values.tps.map(minuteResolution), color: tpsColor, zones: tpsZone, yAxis: 0
            });
            series.cpu.push({
                name: server.serverName, type: spline, tooltip: tooltip.twoDecimals,
                data: server.values.cpu.map(minuteResolution), color: cpuColor, yAxis: 0
            });
            series.ram.push({
                name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
                data: server.values.ram.map(minuteResolution), color: ramColors, yAxis: 0
            });
            series.entities.push({
                name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
                data: server.values.entities.map(minuteResolution), color: entitiesColor, yAxis: 0
            });
            series.chunks.push({
                name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
                data: server.values.chunks.map(minuteResolution), color: chunksColor, yAxis: 0
            });
            series.disk.push({
                name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
                data: server.values.disk.map(minuteResolution), color: diskColor, zones: diskZones, yAxis: 0
            });
            series.msptAverage.push({
                name: server.serverName, type: spline, tooltip: tooltip.twoDecimals,
                data: server.values.msptAverage.map(minuteResolution), color: msptAverageColor, yAxis: 0
            });
            series.mspt95thPercentile.push({
                name: server.serverName, type: spline, tooltip: tooltip.twoDecimals,
                data: server.values.mspt95thPercentile.map(minuteResolution), color: msptPercentileColor, yAxis: 0
            });
        });
        setPerformanceSeries(series);
    }, [data, setPerformanceSeries])

    const dataIncludesGameServers = useMemo(() => data.servers && Boolean(data.servers.filter(server => !server.proxy).length), [data]);

    const tabs = [
        {
            name: t('html.label.playersOnline'), icon: faUser, color: 'players-online', href: 'players-online',
            element: <Tab id={'players-online'} data={performanceSeries.players}
                          yAxis={yAxisConfigurations.PLAYERS_ONLINE}/>
        }, {
            name: t('html.label.tps'), icon: faTachometerAlt, color: 'tps', href: 'tps',
            element: <Tab id={'tps'} data={performanceSeries.tps} yAxis={yAxisConfigurations.TPS}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.msptAverage'), icon: faStopwatch, color: 'mspt-average', href: 'mspt',
            element: <Tab id={'mspt'} data={performanceSeries.msptAverage} yAxis={yAxisConfigurations.MSPT}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.msptPercentile', {percentile: 95}),
            icon: faStopwatch,
            color: 'mspt-percentile',
            href: 'mspt-percentile',
            element: <Tab id={'mspt-percentile'} data={performanceSeries.mspt95thPercentile}
                          yAxis={yAxisConfigurations.MSPT}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.cpu'), icon: faTachometerAlt, color: 'cpu', href: 'cpu',
            element: <Tab id={'cpu'} data={performanceSeries.cpu} yAxis={yAxisConfigurations.CPU}/>
        }, {
            name: t('html.label.ram'), icon: faMicrochip, color: 'ram', href: 'ram',
            element: <Tab id={'ram'} data={performanceSeries.ram} yAxis={yAxisConfigurations.RAM_OR_DISK}/>
        }, {
            name: t('html.label.entities'), icon: faDragon, color: 'entities', href: 'entities',
            element: <Tab id={'entities'} data={performanceSeries.entities} yAxis={yAxisConfigurations.ENTITIES}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.loadedChunks'), icon: faMap, color: 'chunks', href: 'chunks',
            element: <Tab id={'chunks'} data={performanceSeries.chunks} yAxis={yAxisConfigurations.CHUNKS}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.diskSpace'), icon: faHdd, color: 'disk', href: 'disk',
            element: <Tab id={'disk'} data={performanceSeries.disk} yAxis={yAxisConfigurations.RAM_OR_DISK}/>
        }, {
            name: t('html.label.ping'), icon: faSignal, color: 'ping', href: 'ping',
            element: networkMetadata ? <PingTab id={'ping'} identifier={networkMetadata.currentServer.serverUUID}/> :
                <ChartLoader/>
        },
    ];

    if (!data || !Object.values(data).length) return <CardLoader/>
    if (data.errors.length) {
        return <ErrorViewCard error={data.errors[0]}/>
    }
    return (
        <Card>
            <GraphExtremesContextProvider>
                <CardTabs tabs={tabs}/>
            </GraphExtremesContextProvider>
        </Card>
    )
};

export default PerformanceGraphsCard