import React, {useEffect, useMemo, useState} from 'react';
import CardTabs from "../../CardTabs";
import {
    faDragon,
    faHdd,
    faMap,
    faMicrochip,
    faSignal,
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

const Tab = ({data, yAxis}) => {
    return (
        <LineGraph id={'performance-' + new Date().getTime()} series={data} legendEnabled tall yAxis={yAxis}/>
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
        disk: []
    });

    useEffect(() => {
        if (!data?.zones) return;

        const zones = {
            tps: [{
                value: data.zones.tpsThresholdMed,
                color: data.colors.low
            }, {
                value: data.zones.tpsThresholdHigh,
                color: data.colors.med
            }, {
                value: 30,
                color: data.colors.high
            }],
            disk: [{
                value: data.zones.diskThresholdMed,
                color: data.colors.low
            }, {
                value: data.zones.diskThresholdHigh,
                color: data.colors.med
            }, {
                value: Number.MAX_VALUE,
                color: data.colors.high
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
            disk: []
        }

        const spline = 'spline';

        const changeColor = (colorHex, index) => {
            // TODO Convert color somehow using index
            return colorHex;
        }

        const minuteResolution = point => {
            // Ensure that the points can be stacked by moving data to minute level
            point[0] -= (point[0] % 60000);
            return point;
        }

        serverData.forEach((server, i) => {
            const playersOnlineColor = changeColor(data.colors.playersOnline, i);
            const tpsColor = changeColor(data.colors.high, i);
            const tpsZone = [...zones.tps]
            tpsZone.forEach(zone => zone.color = changeColor(zone.color, i));
            const cpuColor = changeColor(data.colors.cpu, i);
            const ramColors = changeColor(data.colors.ram, i);
            const entitiesColor = changeColor(data.colors.entities, i);
            const chunksColor = changeColor(data.colors.chunks, i);
            const diskColor = changeColor(data.colors.high, i);
            const diskZones = [...zones.disk];
            diskZones.forEach(zone => zone.color = changeColor(zone.color, i));

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
        });
        setPerformanceSeries(series);
    }, [data, setPerformanceSeries])

    const dataIncludesGameServers = data.servers && Boolean(data.servers.filter(server => !server.proxy).length);

    const tabs = useMemo(() => [
        {
            name: t('html.label.playersOnline'), icon: faUser, color: 'light-blue', href: 'players-online',
            element: <Tab data={performanceSeries.players} yAxis={yAxisConfigurations.PLAYERS_ONLINE}/>
        }, {
            name: t('html.label.tps'), icon: faTachometerAlt, color: 'red', href: 'tps',
            element: <Tab data={performanceSeries.tps} yAxis={yAxisConfigurations.TPS}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.cpu'), icon: faTachometerAlt, color: 'amber', href: 'cpu',
            element: <Tab data={performanceSeries.cpu} yAxis={yAxisConfigurations.CPU}/>
        }, {
            name: t('html.label.ram'), icon: faMicrochip, color: 'light-green', href: 'ram',
            element: <Tab data={performanceSeries.ram} yAxis={yAxisConfigurations.RAM_OR_DISK}/>
        }, {
            name: t('html.label.entities'), icon: faDragon, color: 'purple', href: 'entities',
            element: <Tab data={performanceSeries.entities} yAxis={yAxisConfigurations.ENTITIES}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.loadedChunks'), icon: faMap, color: 'blue-grey', href: 'chunks',
            element: <Tab data={performanceSeries.chunks} yAxis={yAxisConfigurations.CHUNKS}/>,
            disabled: !dataIncludesGameServers
        }, {
            name: t('html.label.diskSpace'), icon: faHdd, color: 'green', href: 'disk',
            element: <Tab data={performanceSeries.disk} yAxis={yAxisConfigurations.RAM_OR_DISK}/>
        }, {
            name: t('html.label.ping'), icon: faSignal, color: 'amber', href: 'ping',
            element: networkMetadata ? <PingTab identifier={networkMetadata.currentServer.serverUUID}/> :
                <ChartLoader/>
        },
    ], [performanceSeries, networkMetadata, t]);

    if (!data || !Object.values(data).length) return <CardLoader/>
    if (data.errors.length) {
        return <ErrorViewCard error={data.errors[0]}/>
    }
    return (
        <Card>
            <CardTabs tabs={tabs}/>
        </Card>
    )
};

export default PerformanceGraphsCard