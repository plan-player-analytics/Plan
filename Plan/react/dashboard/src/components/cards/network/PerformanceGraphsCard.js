import React from 'react';
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

    if (!data || !Object.values(data).length) return <CardLoader/>

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

    for (const server of serverData) {
        series.players.push({
            name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
            data: server.values.playersOnline, color: data.colors.playersOnline, yAxis: 0
        });
        series.tps.push({
            name: server.serverName, type: spline, tooltip: tooltip.twoDecimals,
            data: server.values.tps, color: data.colors.high, zones: zones.tps, yAxis: 0
        });
        series.cpu.push({
            name: server.serverName, type: spline, tooltip: tooltip.twoDecimals,
            data: server.values.cpu, color: data.colors.cpu, yAxis: 0
        });
        series.ram.push({
            name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
            data: server.values.ram, color: data.colors.ram, yAxis: 0
        });
        series.entities.push({
            name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
            data: server.values.entities, color: data.colors.entities, yAxis: 0
        });
        series.chunks.push({
            name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
            data: server.values.chunks, color: data.colors.chunks, yAxis: 0
        });
        series.disk.push({
            name: server.serverName, type: spline, tooltip: tooltip.zeroDecimals,
            data: server.values.disk, color: data.colors.high, zones: zones.disk, yAxis: 0
        });
    }

    if (data.errors.length) {
        return <ErrorViewCard error={data.errors[0]}/>
    }

    return (
        <Card>
            <CardTabs tabs={[
                {
                    name: t('html.label.playersOnline'), icon: faUser, color: 'light-blue', href: 'players-online',
                    element: <Tab data={series.players} yAxis={yAxisConfigurations.PLAYERS_ONLINE}/>
                }, {
                    name: t('html.label.tps'), icon: faTachometerAlt, color: 'red', href: 'tps',
                    element: <Tab data={series.tps} yAxis={yAxisConfigurations.TPS}/>
                }, {
                    name: t('html.label.cpu'), icon: faTachometerAlt, color: 'amber', href: 'cpu',
                    element: <Tab data={series.cpu} yAxis={yAxisConfigurations.CPU}/>
                }, {
                    name: t('html.label.ram'), icon: faMicrochip, color: 'light-green', href: 'ram',
                    element: <Tab data={series.ram} yAxis={yAxisConfigurations.RAM_OR_DISK}/>
                }, {
                    name: t('html.label.entities'), icon: faDragon, color: 'purple', href: 'entities',
                    element: <Tab data={series.entities} yAxis={yAxisConfigurations.ENTITIES}/>
                }, {
                    name: t('html.label.loadedChunks'), icon: faMap, color: 'blue-grey', href: 'chunks',
                    element: <Tab data={series.chunks} yAxis={yAxisConfigurations.CHUNKS}/>
                }, {
                    name: t('html.label.diskSpace'), icon: faHdd, color: 'green', href: 'disk',
                    element: <Tab data={series.disk} yAxis={yAxisConfigurations.RAM_OR_DISK}/>
                }, {
                    name: t('html.label.ping'), icon: faSignal, color: 'amber', href: 'ping',
                    element: networkMetadata ? <PingTab identifier={networkMetadata.currentServer.serverUUID}/> :
                        <ChartLoader/>
                },
            ]}/>
        </Card>
    )
};

export default PerformanceGraphsCard