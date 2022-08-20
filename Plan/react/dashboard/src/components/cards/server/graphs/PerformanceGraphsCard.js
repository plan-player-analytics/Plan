import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchOptimizedPerformance, fetchPingGraph} from "../../../../service/serverService";
import {ErrorViewBody} from "../../../../views/ErrorView";
import {useTranslation} from "react-i18next";
import {Card} from "react-bootstrap-v5";
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

const AllGraphTab = ({data, dataSeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <AllPerformanceGraph id="server-performance-all-chart" data={data} dataSeries={dataSeries}/>
}

const TpsGraphTab = ({data, dataSeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <TpsPerformanceGraph id="server-performance-tps-chart" data={data} dataSeries={dataSeries}/>
}

const CpuRamGraphTab = ({data, dataSeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <CpuRamPerformanceGraph id="server-performance-cpuram-chart" data={data} dataSeries={dataSeries}/>
}

const WorldGraphTab = ({data, dataSeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <WorldPerformanceGraph id="server-performance-world-chart" data={data} dataSeries={dataSeries}/>
}

const DiskGraphTab = ({data, dataSeries, loadingError}) => {
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!dataSeries) return <ChartLoader style={{height: "450px"}}/>;

    return <DiskPerformanceGraph id="server-performance-disk-chart" data={data} dataSeries={dataSeries}/>
}

const PingGraphTab = ({identifier}) => {
    const {data, loadingError} = useDataRequest(fetchPingGraph, [identifier]);
    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <ChartLoader style={{height: "450px"}}/>;

    return <PingGraph id="server-performance-ping-chart" data={data}/>;
}

function mapToDataSeries(performanceData) {
    const playersOnline = [];
    const tps = [];
    const cpu = [];
    const ram = [];
    const entities = [];
    const chunks = [];
    const disk = [];

    return new Promise((resolve => {
        let i = 0;
        const length = performanceData.length;

        function processNextThousand() {
            const to = Math.min(i + 1000, length);
            for (i; i < to; i++) {
                const entry = performanceData[i];
                const date = entry[0];
                playersOnline[i] = [date, entry[1]];
                tps[i] = [date, entry[2]];
                cpu[i] = [date, entry[3]];
                ram[i] = [date, entry[4]];
                entities[i] = [date, entry[5]];
                chunks[i] = [date, entry[6]];
                disk[i] = [date, entry[7]];
            }
            if (i >= length) {
                resolve({playersOnline, tps, cpu, ram, entities, chunks, disk})
            } else {
                setTimeout(processNextThousand, 10);
            }
        }

        processNextThousand();
    }))
}

const PerformanceGraphsCard = () => {
    const {t} = useTranslation();

    const {identifier} = useParams();
    const {data, loadingError} = useDataRequest(fetchOptimizedPerformance, [identifier]);
    const [parsedData, setParsedData] = useState(undefined)

    useEffect(() => {
        if (data) {
            mapToDataSeries(data.values).then(parsed => setParsedData(parsed))
        }
    }, [data, setParsedData]);

    return <Card>
        <CardTabs tabs={[
            {
                name: t('html.label.all'), icon: faGears, color: 'blue-grey', href: 'all',
                element: <AllGraphTab data={data} dataSeries={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.tps'), icon: faTachometerAlt, color: 'red', href: 'tps',
                element: <TpsGraphTab data={data} dataSeries={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.cpuRam'), icon: faMicrochip, color: 'light-green', href: 'cpu-ram',
                element: <CpuRamGraphTab data={data} dataSeries={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.world'), icon: faMap, color: 'purple', href: 'world-load',
                element: <WorldGraphTab data={data} dataSeries={parsedData} loadingError={loadingError}/>
            }, {
                name: t('html.label.ping'), icon: faSignal, color: 'amber', href: 'ping',
                element: <PingGraphTab identifier={identifier}/>
            }, {
                name: t('html.label.diskSpace'), icon: faHdd, color: 'green', href: 'disk',
                element: <DiskGraphTab data={data} dataSeries={parsedData} loadingError={loadingError}/>
            },
        ]}/>
    </Card>
}

export default PerformanceGraphsCard;