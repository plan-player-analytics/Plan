import React, {useEffect, useState} from 'react';
import PunchCard from "../../graphs/PunchCard.jsx";
import player from '../../../mockdata/player.json';
import geolocations from '../../../mockdata/geolocations.json';
import PlayersOnlineGraph from "../../graphs/PlayersOnlineGraph.jsx";
import TpsPerformanceGraph from "../../graphs/performance/TpsPerformanceGraph.jsx";
import CpuRamPerformanceGraph from "../../graphs/performance/CpuRamPerformanceGraph.jsx";
import WorldPerformanceGraph from "../../graphs/performance/WorldPerformanceGraph.jsx";
import DiskPerformanceGraph from "../../graphs/performance/DiskPerformanceGraph.jsx";
import PingGraph from "../../graphs/performance/PingGraph.jsx";
import GeolocationsCard from "../../cards/common/GeolocationsCard.jsx";
import {calculateCssHexColor} from "../../../util/colors.js";

const randomDate = (start, end) => {
    return new Date(start.getTime() + Math.random() * (end.getTime() - start.getTime()));
}

const generatePoints = (rangeStart, rangeEnd) => {
    const arr = [];
    const start = new Date(new Date().getTime() - (24 * 60 * 6000));
    const startRange = rangeStart || 0;
    const endRange = rangeEnd ? (rangeEnd - startRange) : 100;
    for (let i = 0; i < 100; i++) {
        const date = randomDate(start, new Date());
        const randNum = startRange + Math.round(Math.random() * endRange);
        arr.push([date.getTime(), randNum]);
    }

    arr.sort(function (a, b) {
        if (a[0] < b[0]) return -1;
        if (a[0] > b[0]) return 1;
        return 0;
    })

    return arr;
}

const regeneratePoints = (points, rangeStart, rangeEnd) => {
    const newPoints = []
    for (let i = points.length - 1; i >= 0; i--) {
        const randNum = (rangeStart || 0) + Math.round(Math.random() * (rangeEnd || 100));
        newPoints.push([points[i][0], randNum]);
    }
    return newPoints;
}

const PunchCardGraphUseCase = () => {
    return <PunchCard series={player.punchcard_series}/>
};

const PlayersOnlineGraphUseCase = () => {
    return <PlayersOnlineGraph data={{playersOnline: generatePoints()}} selectedRange={4}/>
}

const TpsGraphUseCase = () => {
    return <TpsPerformanceGraph id={'tps-graph'} data={{zones: {tpsThresholdHigh: 15, tpsThresholdMed: 5}}}
                                dataSeries={{playersOnline: [], tps: generatePoints(0, 20)}}
                                pluginHistorySeries={{}}/>
}
const CpuGraphUseCase = () => {
    const dataSeries = {
        playersOnline: [],
        cpu: generatePoints(0, 100),
        ram: []
    };
    return <CpuRamPerformanceGraph id={'cpu-graph'}
                                   dataSeries={dataSeries}
                                   pluginHistorySeries={{}}/>
}

const RamGraphUseCase = () => {
    const dataSeries = {
        playersOnline: [],
        cpu: [],
        ram: generatePoints(10000, 11000)
    };
    return <CpuRamPerformanceGraph id={'ram-graph'}
                                   dataSeries={dataSeries}
                                   pluginHistorySeries={{}}/>
}

const ChunksGraphUseCase = () => {
    const dataSeries = {
        playersOnline: [],
        chunks: generatePoints(100, 2000),
        entities: []
    };
    return <WorldPerformanceGraph id={'chunk-graph'}
                                  dataSeries={dataSeries}
                                  pluginHistorySeries={{}}/>
}

const EntitiesGraphUseCase = () => {
    const dataSeries = {
        playersOnline: [],
        chunks: [],
        entities: generatePoints(100, 20000)
    };
    return <WorldPerformanceGraph id={'entities-graph'}
                                  dataSeries={dataSeries}
                                  pluginHistorySeries={{}}/>
}

const DiskGraphUseCase = () => {
    const dataSeries = {
        disk: generatePoints(50, 2000),
    }
    return <DiskPerformanceGraph id={'disk-graph'} dataSeries={dataSeries}
                                 data={{zones: {diskThresholdHigh: 500, diskThresholdMed: 100}}}
                                 pluginHistorySeries={{}}/>
}

const PingGraphUseCase = () => {
    const points = generatePoints(0, 25);
    const data = {
        min_ping_series: points,
        avg_ping_series: regeneratePoints(points, 50, 75),
        max_ping_series: regeneratePoints(points, 100, 200),
    }
    return <PingGraph id={'ping-graph'} data={data}/>
}

const WorldMapUseCase = () => {
    const [identifier, setIdentifier] = useState(0);
    const [currentMinColor, setCurrentMinColor] = useState(calculateCssHexColor("var(--color-graphs-world-map-low)"))
    const [currentMaxColor, setCurrentMaxColor] = useState(calculateCssHexColor("var(--color-graphs-world-map-high)"))
    useEffect(() => {
        const interval = setInterval(() => {
            const minColor = calculateCssHexColor("var(--color-graphs-world-map-low)");
            const maxColor = calculateCssHexColor("var(--color-graphs-world-map-high)");
            if (minColor !== currentMinColor) {
                setIdentifier(identifier + 1);
                setCurrentMinColor(minColor);
            }
            if (maxColor !== currentMaxColor) {
                setIdentifier(identifier + 1);
                setCurrentMaxColor(maxColor);
            }
        }, 1000);
        return () => {
            clearInterval(interval);
        }
    }, [identifier]);

    return <GeolocationsCard identifier={identifier} data={geolocations}/>
}

export const graphUseCases = {
    'graphs.punchCard': <PunchCardGraphUseCase/>,
    'graphs.playersOnline': <PlayersOnlineGraphUseCase/>,
    'graphs.tps': <TpsGraphUseCase/>,
    'graphs.cpu': <CpuGraphUseCase/>,
    'graphs.ram': <RamGraphUseCase/>,
    'graphs.chunks': <ChunksGraphUseCase/>,
    'graphs.entities': <EntitiesGraphUseCase/>,
    'graphs.disk': <DiskGraphUseCase/>,
    'graphs.ping': <PingGraphUseCase/>,
    'graphs.worldMap': <WorldMapUseCase/>,
}