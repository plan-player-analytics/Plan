import React, { useEffect, useState } from 'react';
import PunchCard from '../../graphs/PunchCard.jsx';
import player from '../../../mockdata/player.json';
import geolocations from '../../../mockdata/geolocations.json';
import PlayersOnlineGraph from '../../graphs/PlayersOnlineGraph.jsx';
import GeolocationsCard from '../../cards/common/GeolocationsCard.jsx';
import { GraphSeriesCard } from '../../cards/server/graphs/GraphSeriesCard';
import { calculateCssHexColor } from '../../../util/colors.js';
import Background from './Background';
import { ParsedPerformanceGraph } from '../../../dataHooks/model/graph/performance/PerformanceGraph';
import { LineSeries } from '../../../dataHooks/model/graph/LineSeries';
import { ParsedPerformanceZones } from '../../../dataHooks/model/graph/performance/PerformanceZones';
import { PingGraph } from '../../../dataHooks/model/graph/performance/PingGraph';

const randomDate = (start: Date, end: Date) => {
    return new Date(
        start.getTime() + Math.random() * (end.getTime() - start.getTime()),
    );
};

const generatePoints = (rangeStart?: number, rangeEnd?: number): LineSeries => {
    const arr = [];
    const start = new Date(Date.now() - 2 * 86400000);
    const startRange = rangeStart || 0;
    const endRange = rangeEnd ? rangeEnd - startRange : 100;
    for (let i = 0; i < 100; i++) {
        const date = randomDate(start, new Date());
        const randNum = startRange + Math.round(Math.random() * endRange);
        arr.push([date.getTime(), randNum]);
    }

    arr.sort(function (a, b) {
        if (a[0] < b[0]) return -1;
        if (a[0] > b[0]) return 1;
        return 0;
    });

    return arr as [number, number, ...number[]][];
};

const regeneratePoints = (
    points: LineSeries,
    rangeStart: number,
    rangeEnd: number,
): LineSeries => {
    const newPoints = [];
    for (let i = points.length - 1; i >= 0; i--) {
        const randNum =
            (rangeStart || 0) + Math.round(Math.random() * (rangeEnd || 100));
        newPoints.push([points[i][0], randNum]);
    }
    return newPoints as [number, number, ...number[]][];
};

const PunchCardGraphUseCase = () => {
    return (
        <Background>
            <PunchCard series={player.punchcardSeries} />
        </Background>
    );
};

const PlayersOnlineGraphUseCase = () => {
    return (
        <Background>
            <PlayersOnlineGraph
                data={{ playersOnline: generatePoints() }}
                selectedRange={4}
                color={undefined}
                extremes={undefined}
                identifier={undefined}
                onSetExtremes={undefined}
                showPlayersOnline={false}
            />
        </Background>
    );
};

const commonPerformanceSeries: ParsedPerformanceGraph = {
    playersOnline: generatePoints(10, 50),
    tps: generatePoints(0, 20),
    cpu: generatePoints(0, 100),
    ram: generatePoints(10000, 11000),
    entities: generatePoints(100, 20000),
    chunks: generatePoints(100, 2000),
    disk: generatePoints(50, 2000),
    msptAverage: generatePoints(0, 50),
    mspt95thPercentile: generatePoints(0, 50),
    msptJitterAverage: generatePoints(0, 25),
    msptJitterMax: generatePoints(0, 50),
};

const commonPingSeries = (): PingGraph => {
    const points = generatePoints(0, 25);
    return {
        minPingSeries: points,
        avgPingSeries: regeneratePoints(points, 50, 75),
        maxPingSeries: regeneratePoints(points, 100, 200),
    };
};

const commonZones: ParsedPerformanceZones = {
    tps: [
        {
            value: 5,
            color: 'var(--color-graphs-tps-low)',
        },
        {
            value: 15,
            color: 'var(--color-graphs-tps-medium)',
        },
        {
            value: 30,
            color: 'var(--color-graphs-tps-high)',
        },
    ],
    disk: [
        {
            value: 100,
            color: 'var(--color-graphs-disk-low)',
        },
        {
            value: 500,
            color: 'var(--color-graphs-disk-medium)',
        },
        {
            value: Number.MAX_VALUE,
            color: 'var(--color-graphs-disk-high)',
        },
    ],
};

const TpsGraphUseCase = () => {
    return (
        <Background>
            <GraphSeriesCard
                id={'tps-graph'}
                zones={commonZones}
                performanceSeries={commonPerformanceSeries}
                pingSeries={commonPingSeries()}
                defaultEnabledSeries={['tps']}
            />
        </Background>
    );
};

const CpuGraphUseCase = () => {
    return (
        <Background>
            <GraphSeriesCard
                id={'cpu-graph'}
                zones={commonZones}
                performanceSeries={commonPerformanceSeries}
                pingSeries={commonPingSeries()}
                defaultEnabledSeries={['cpu', 'ram']}
            />
        </Background>
    );
};

const RamGraphUseCase = () => {
    return (
        <Background>
            <GraphSeriesCard
                id={'ram-graph'}
                zones={commonZones}
                performanceSeries={commonPerformanceSeries}
                pingSeries={commonPingSeries()}
                defaultEnabledSeries={['cpu', 'ram']}
            />
        </Background>
    );
};

const ChunksGraphUseCase = () => {
    return (
        <Background>
            <GraphSeriesCard
                id={'chunk-graph'}
                zones={commonZones}
                performanceSeries={commonPerformanceSeries}
                pingSeries={commonPingSeries()}
                defaultEnabledSeries={['chunks', 'entities']}
            />
        </Background>
    );
};

const EntitiesGraphUseCase = () => {
    return (
        <Background>
            <GraphSeriesCard
                id={'entities-graph'}
                zones={commonZones}
                performanceSeries={commonPerformanceSeries}
                pingSeries={commonPingSeries()}
                defaultEnabledSeries={['chunks', 'entities']}
            />
        </Background>
    );
};

const DiskGraphUseCase = () => {
    return (
        <Background>
            <GraphSeriesCard
                id={'disk-graph'}
                zones={commonZones}
                performanceSeries={commonPerformanceSeries}
                pingSeries={commonPingSeries()}
                defaultEnabledSeries={['disk']}
            />
        </Background>
    );
};

const PingGraphUseCase = () => {
    return (
        <Background>
            <GraphSeriesCard
                id={'ping-graph'}
                zones={commonZones}
                performanceSeries={commonPerformanceSeries}
                pingSeries={commonPingSeries()}
                defaultEnabledSeries={['ping']}
            />
        </Background>
    );
};

const WorldMapUseCase = () => {
    const [identifier, setIdentifier] = useState(0);
    const [currentMinColor, setCurrentMinColor] = useState(
        calculateCssHexColor('var(--color-graphs-world-map-low)'),
    );
    const [currentMaxColor, setCurrentMaxColor] = useState(
        calculateCssHexColor('var(--color-graphs-world-map-high)'),
    );
    useEffect(() => {
        const interval = setInterval(() => {
            const minColor = calculateCssHexColor(
                'var(--color-graphs-world-map-low)',
            );
            const maxColor = calculateCssHexColor(
                'var(--color-graphs-world-map-high)',
            );
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
        };
    }, [identifier]);

    return <GeolocationsCard identifier={identifier} data={geolocations} />;
};

export const graphUseCases = {
    'graphs.style': <PlayersOnlineGraphUseCase />,
    'graphs.punchCard': <PunchCardGraphUseCase />,
    'graphs.playersOnline': <PlayersOnlineGraphUseCase />,
    'graphs.tps': <TpsGraphUseCase />,
    'graphs.cpu': <CpuGraphUseCase />,
    'graphs.ram': <RamGraphUseCase />,
    'graphs.chunks': <ChunksGraphUseCase />,
    'graphs.entities': <EntitiesGraphUseCase />,
    'graphs.disk': <DiskGraphUseCase />,
    'graphs.ping': <PingGraphUseCase />,
    'graphs.worldMap': <WorldMapUseCase />,
};
