import {Card, CardBody, CardFooter} from "react-bootstrap";
import React, {useEffect, useMemo, useState} from "react";
import {
    faDragon,
    faGear,
    faHdd,
    faMap,
    faMicrochip,
    faSignal,
    faStopwatch,
    faTachometerAlt,
    faUser,
    faWaveSquare
} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useAuth} from "../../../../hooks/authenticationHook";
import OutlineButton from "../../../input/button/OutlineButton";
import {faRectangleXmark} from "@fortawesome/free-regular-svg-icons";
import {usePerformanceGraphData} from "../../../../dataHooks/graph/usePerformanceGraphData";
import {tooltip, translateLinegraphButtons} from "../../../../util/graphs";
import {usePingFormatter} from "../../../../util/format/usePingFormatter";
import {useMetadata} from "../../../../hooks/metadataHook";
import {useTheme} from "../../../../hooks/themeHook";
import {useI18nFriendlyLanguage} from "../../../../service/localeService";
import {PerformanceGraphId} from "../../../../dataHooks/model/graph/performance/PerformanceGraphId";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/no-data-to-display"
import "highcharts/esm/modules/accessibility";
import {YAxisById} from "../../../../dataHooks/model/graph/YAxis";
import {ErrorViewBody} from "../../../../views/ErrorView";
import {GraphPreset} from "../../../../dataHooks/model/graph/GraphPreset";
import {GraphSeries} from "../../../../dataHooks/model/graph/GraphSeries";

type Props = {
    id: string;
    identifier: string;
}

type EnabledSeries = { [key: string]: boolean };

// TODO use this graph in Theme Examples
export const GraphSeriesCard = ({id, identifier}: Props) => {
    const {t} = useTranslation();
    const {hasPermission} = useAuth();
    const {formatPing} = usePingFormatter();
    const metadata = useMetadata();
    const {graphTheming, nightModeEnabled} = useTheme();
    const {
        zones,
        performanceSeries,
        pluginHistorySeries,
        pingSeries,
        loadingError
    } = usePerformanceGraphData({identifier});

    const [enabledSeries, setEnabledSeries] = useState<EnabledSeries>({});
    const [graph, setGraph] = useState<Highcharts.StockChart | undefined>(undefined);

    const drawSeries = (series: EnabledSeries) => {
        if (!graph) return;
        const newSeries = getActiveSeries(series);
        while (graph.series.length) {
            graph.series[0].remove(false);
        }
        newSeries.forEach(series => {
            graph.addSeries(series as Highcharts.SeriesOptionsType, false, false);
        });
        graph.redraw(false);
    }

    const toggleSeries = (id: string) => {
        let changed = {...enabledSeries}
        if (enabledSeries[id]) {
            delete changed[id];
        } else {
            changed = {...enabledSeries, [id]: true};
        }
        drawSeries(changed);
        setEnabledSeries(changed);
    }
    const enablePreset = (series: string[]) => {
        const enabled: EnabledSeries = {};
        for (const id of series) {
            enabled[id] = true;
        }
        let changed;
        if (JSON.stringify(enabled) === JSON.stringify(enabledSeries)) {
            changed = {};
        } else {
            const enabled: EnabledSeries = {};
            for (const id of series) {
                enabled[id] = true;
            }
            changed = enabled;
        }
        drawSeries(changed);
        setEnabledSeries(changed);
    }

    const presets: GraphPreset[] = [{
        name: t('html.label.tps'), icon: faTachometerAlt, color: 'tps',
        permission: 'page.server.performance.graphs.tps',
        series: [PerformanceGraphId.TPS, PerformanceGraphId.MSPT_95TH, PerformanceGraphId.MSPT_JITTER_MAX, PerformanceGraphId.PLAYERS_ONLINE]
    }, {
        name: t('html.label.mspt'), icon: faStopwatch, color: 'mspt-average',
        permission: 'page.server.performance.graphs.mspt',
        series: [PerformanceGraphId.MSPT_AVG, PerformanceGraphId.MSPT_95TH, PerformanceGraphId.MSPT_JITTER_AVG, PerformanceGraphId.MSPT_JITTER_MAX]
    }, {
        name: t('html.label.world'), icon: faMap, color: 'entities',
        permission: ['page.server.performance.graphs.entities', 'page.server.performance.graphs.chunks'],
        series: [PerformanceGraphId.CHUNKS, PerformanceGraphId.ENTITIES]
    }, {
        name: t('html.label.cpuRam'), icon: faMicrochip, color: 'ram',
        permission: ['page.server.performance.graphs.cpu', 'page.server.performance.graphs.ram'],
        series: [PerformanceGraphId.CPU, PerformanceGraphId.RAM]
    }, {
        name: t('html.label.diskSpace'), icon: faHdd, color: 'disk',
        permission: 'page.server.performance.graphs.disk',
        series: [PerformanceGraphId.DISK]
    }, {
        name: t('html.label.ping'), icon: faSignal, color: 'ping',
        permission: 'page.server.performance.graphs.ping',
        series: [PerformanceGraphId.PING]
    }];

    const yAxis: YAxisById = {
        players: {
            labels: {format: '{value} P', style: {color: 'var(--color-graphs-players-online)'}},
            softMin: 0,
            softMax: 2
        },
        tps: {
            labels: {format: `{value} ${t('html.label.tps')}`, style: {color: 'var(--color-graphs-tps-high)'}},
            softMin: 0,
            softMax: 20
        },
        milliseconds: {
            labels: {
                formatter: function () {
                    return formatPing('value' in this && this.value);
                }
            },
            softMin: 0,
            softMax: 50
        },
        percentage: {
            labels: {format: '{value} %'},
            softMin: 0,
            softMax: 100
        },
        megabytes: {
            labels: {format: '{value} MB'},
            softMin: 0
        },
        chunks: {
            labels: {format: '{value} C', style: {color: 'var(--color-graphs-chunks)'}},
            softMin: 0
        },
        entities: {
            labels: {format: '{value} E', style: {color: 'var(--color-graphs-entities)'}},
            softMin: 0
        }
    }


    const series: GraphSeries[] = useMemo(() => {
        return [{
            id: PerformanceGraphId.PLAYERS_ONLINE,
            name: t('html.label.players'), icon: faUser, color: 'players-online',
            permission: 'page.server.performance.graphs.players.online',
            data: performanceSeries?.playersOnline,
            options: {
                type: 'areaspline',
                tooltip: tooltip.zeroDecimals,
                yAxis: 'players'
            }
        }, {
            id: PerformanceGraphId.TPS,
            name: t('html.label.tps'), icon: faTachometerAlt, color: 'tps',
            permission: 'page.server.performance.graphs.tps',
            data: performanceSeries?.tps,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'tps'
            }
        }, {
            id: PerformanceGraphId.MSPT_AVG,
            name: t('html.label.msptAverage'), icon: faStopwatch, color: 'mspt-average',
            permission: 'page.server.performance.graphs.mspt',
            data: performanceSeries?.msptAverage,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'milliseconds'
            }
        }, {
            id: PerformanceGraphId.MSPT_95TH,
            name: t('html.label.msptPercentile', {percentile: 95}), icon: faStopwatch, color: 'mspt-percentile',
            permission: 'page.server.performance.graphs.mspt',
            data: performanceSeries?.mspt95thPercentile,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'milliseconds'
            }
        }, {
            id: PerformanceGraphId.MSPT_JITTER_AVG,
            name: t('html.label.msptJitterAverage'),
            icon: faWaveSquare, color: 'mspt-average',
            permission: 'page.server.performance.graphs.mspt',
            data: performanceSeries?.msptJitterAverage,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'milliseconds'
            }
        }, {
            id: PerformanceGraphId.MSPT_JITTER_MAX,
            name: t('html.label.msptJitterMax'),
            icon: faWaveSquare, color: 'mspt-percentile',
            permission: 'page.server.performance.graphs.mspt',
            data: performanceSeries?.msptJitterMax,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'milliseconds'
            }
        }, {
            id: PerformanceGraphId.ENTITIES,
            name: t('html.label.entities'), icon: faDragon, color: 'entities',
            permission: 'page.server.performance.graphs.entities',
            data: performanceSeries?.entities,
            options: {
                type: 'spline',
                tooltip: tooltip.zeroDecimals,
                yAxis: 'entities'
            }
        }, {
            id: PerformanceGraphId.CHUNKS,
            name: t('html.label.loadedChunks'), icon: faMap, color: 'chunks',
            permission: 'page.server.performance.graphs.chunks',
            data: performanceSeries?.chunks,
            options: {
                type: 'spline',
                tooltip: tooltip.zeroDecimals,
                yAxis: 'chunks'
            }
        }, {
            id: PerformanceGraphId.CPU,
            name: t('html.label.cpu'), icon: faMicrochip, color: 'cpu',
            permission: 'page.server.performance.graphs.cpu',
            data: performanceSeries?.cpu,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'percentage'
            }
        }, {
            id: PerformanceGraphId.RAM,
            name: t('html.label.ram'), icon: faMicrochip, color: 'ram',
            permission: 'page.server.performance.graphs.ram',
            data: performanceSeries?.ram,
            options: {
                type: 'spline',
                tooltip: tooltip.zeroDecimals,
                yAxis: 'megabytes'
            }
        }, {
            id: PerformanceGraphId.DISK,
            name: t('html.label.diskSpace'), icon: faHdd, color: 'disk',
            permission: 'page.server.performance.graphs.disk',
            data: performanceSeries?.disk,
            options: {
                type: 'areaspline',
                tooltip: tooltip.zeroDecimals,
                yAxis: 'megabytes'
            }
        }, {
            id: PerformanceGraphId.PING,
            series: [PerformanceGraphId.PING_MIN, PerformanceGraphId.PING_AVG, PerformanceGraphId.PING_MAX],
            name: t('html.label.ping'), icon: faSignal, color: 'ping',
            permission: 'page.server.performance.graphs.ping',
            data: pingSeries ? [] : undefined
        }, {
            show: false,
            id: PerformanceGraphId.PING_MIN,
            name: t('html.label.bestPing'), icon: faSignal, color: 'ping',
            permission: 'page.server.performance.graphs.ping',
            data: pingSeries?.min_ping_series,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'milliseconds'
            }
        }, {
            show: false,
            id: PerformanceGraphId.PING_AVG,
            name: t('html.label.averagePing'), icon: faSignal, color: 'ping',
            permission: 'page.server.performance.graphs.ping',
            data: pingSeries?.avg_ping_series,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'milliseconds'
            }
        }, {
            show: false,
            id: PerformanceGraphId.PING_MAX,
            name: t('html.label.worstPing'), icon: faSignal, color: 'ping',
            permission: 'page.server.performance.graphs.ping',
            data: pingSeries?.max_ping_series,
            options: {
                type: 'spline',
                tooltip: tooltip.twoDecimals,
                yAxis: 'milliseconds'
            }
        }]
    }, [performanceSeries, t]);

    const locale = useI18nFriendlyLanguage();
    useEffect(() => {
        Highcharts.setOptions({
            lang: {
                locale: locale,
                noData: t('html.label.noDataToDisplay')
            }
        })
    }, [locale]);

    const getActiveSeries = (enabledSeries: EnabledSeries) => {
        const activeSeries = Object.keys(enabledSeries)
            .map(id => series.find(series => series.id === id))
            .filter(s => s !== undefined)
            // Some series can be aggregates, open them up.
            .flatMap(s => 'series' in s ? s.series.map(id => series.find(ser => ser.id === id)) : [s]);

        const newSeries: Highcharts.SeriesOptions[] = activeSeries
            .filter(s => s !== undefined)
            .filter(s => 'data' in s)
            .map(series => ({
                data: series.data?.filter(data => data[1] !== null).length ? series.data : [],
                name: series.name,
                type: series.options.type,
                tooltip: series.options.tooltip,
                color: `var(--color-graphs-${series.color})`,
                zones: zones[series.id] || undefined,
                yAxis: Object.entries(yAxis).findIndex(([id]) => id === series.options.yAxis)
            }));
        const cleanSeries = newSeries.map(obj =>
            Object.fromEntries(
                Object.entries(obj).filter(([, value]) => value !== undefined)
            )
        );
        if (pluginHistorySeries) { // @ts-ignore
            cleanSeries.push(pluginHistorySeries);
        }

        return cleanSeries;
    }

    const chart: Highcharts.Options = useMemo(() => {
        return {
            chart: {
                animation: false
            },
            rangeSelector: {
                selected: 2,
                buttons: translateLinegraphButtons(t) as Highcharts.RangeSelectorButtonsOptions[]
            },
            yAxis: Object.values(yAxis),
            title: {text: ''},
            plotOptions: {
                areaspline: {
                    fillOpacity: nightModeEnabled ? 0.2 : 0.3
                },
                series: {
                    animation: false
                }
            },
            legend: {
                enabled: false
            },
            time: {
                timezoneOffset: metadata.loaded ? metadata.timeZoneOffsetMinutes : 0
            },
            series: getActiveSeries(enabledSeries)
        }
    }, [series, nightModeEnabled]);
    useEffect(() => {
        Highcharts.setOptions(graphTheming);
        setGraph(Highcharts.stockChart(id, chart));
    }, [graphTheming, metadata, chart]);

    return (
        <div style={{display: "flex",}}>
            {!!presets.length && <ul className={"nav flex-column"}>
                {presets
                    .filter((preset) => hasPermission(preset.permission))
                    .map(preset => (
                        <OutlineButton key={preset.name} id={preset.name}
                                       className={"mb-1"}
                                       onClick={() => enablePreset(preset.series)}
                                       style={{textAlign: 'left', backgroundColor: "var(--color-cards-background)"}}
                        >
                            <FontAwesomeIcon icon={preset.icon} className={"col-" + preset.color}/> {preset.name}
                        </OutlineButton>
                    ))}
                {!!Object.values(enabledSeries).length && <OutlineButton id="clear"
                                                                         className={"mt-1 mb-1"}
                                                                         onClick={() => enablePreset([])}
                                                                         style={{
                                                                             textAlign: 'left',
                                                                             backgroundColor: "var(--color-cards-background)"
                                                                         }}
                >
                    <FontAwesomeIcon icon={faRectangleXmark}/> {t('html.generic.none')}
                </OutlineButton>}
            </ul>}
            <Card className={"w-100"}>
                {loadingError && <ErrorViewBody error={loadingError}/>}
                <CardBody className={"chart-area-performance"} id={id}>
                </CardBody>
                <CardFooter>
                    <div style={{
                        display: 'flex', justifyContent: 'center', flexWrap: 'wrap'
                    }}>
                        {series
                            .filter(s => s.show || s.show === undefined)
                            .filter(s => hasPermission(s.permission))
                            .map(series => (
                                <button key={series.id} className={"btn"}
                                        style={{
                                            opacity: enabledSeries[series.id] ? 1 : 0.5,
                                            padding: "0.5rem",
                                            textDecoration: !('data' in series) || series.data?.filter(data => data[1] !== null).length ? "none" : "line-through",
                                        }}
                                        onClick={() => toggleSeries(series.id)}>
                                    <FontAwesomeIcon icon={'data' in series && series.data ? series.icon : faGear}
                                                     className={"col-" + series.color + ('data' in series && series.data ? '' : ' fa-spin')}/>
                                    {' '}
                                    <small>{series.name}</small>
                                </button>
                            ))}
                    </div>
                </CardFooter>
            </Card>
        </div>
    )
}