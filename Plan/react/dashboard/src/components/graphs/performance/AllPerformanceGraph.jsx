import React, {useCallback, useEffect, useState} from 'react';

import {hasValuesInSeries, tooltip, translateLinegraphButtons} from "../../../util/graphs";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/no-data-to-display"
import "highcharts/esm/modules/accessibility";
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import {useMetadata} from "../../../hooks/metadataHook";
import {useAuth} from "../../../hooks/authenticationHook.tsx";
import {useGraphExtremesContext} from "../../../hooks/interaction/graphExtremesContextHook.jsx";
import {localeService} from "../../../service/localeService.js";

const AllPerformanceGraph = ({id, data, dataSeries, pluginHistorySeries}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    const {timeZoneOffsetMinutes} = useMetadata();
    const {hasPermission} = useAuth();
    const {extremes, onSetExtremes} = useGraphExtremesContext();
    const [graph, setGraph] = useState(undefined);

    const yAxis = [
        {
            labels: {
                formatter: function () {
                    return this.value + ' P';
                }
            },
            softMin: 0,
            softMax: 2
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' ' + t('html.label.tps');
                }
            },
            softMin: 0,
            softMax: 20
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + '%';
                }
            },
            softMin: 0,
            softMax: 100
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' MB';
                }
            },
            softMin: 0
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' E';
                }
            },
            softMin: 0,
            softMax: 2
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' C';
                }
            },
            softMin: 0
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return localeService.localizePing(this.value);
                }
            },
            softMin: 0,
            softMax: 50
        },
    ]

    const onResize = useCallback(() => {
        let chartElement = document.getElementById(id);
        let chartId = chartElement?.getAttribute('data-highcharts-chart');
        const chart = chartId !== undefined ? Highcharts.charts[chartId] : undefined;

        if (chart?.yAxis?.length) {
            const newWidth = window.innerWidth
            chart.yAxis[0].update({labels: {enabled: newWidth >= 900 && hasPermission('page.server.performance.graphs.players.online')}});
            chart.yAxis[1].update({labels: {enabled: newWidth >= 900 && hasPermission('page.server.performance.graphs.tps')}});
            chart.yAxis[2].update({labels: {enabled: newWidth >= 1000 && hasPermission('page.server.performance.graphs.cpu')}});
            chart.yAxis[3].update({labels: {enabled: newWidth >= 1000 && hasPermission('page.server.performance.graphs.ram')}});
            chart.yAxis[4].update({labels: {enabled: newWidth >= 1400 && hasPermission('page.server.performance.graphs.entities')}});
            chart.yAxis[5].update({labels: {enabled: newWidth >= 1400 && hasPermission('page.server.performance.graphs.chunks')}});
            chart.yAxis[6].update({labels: {enabled: newWidth >= 900 && hasPermission('page.server.performance.graphs.mspt')}});
        }
    }, [id, hasPermission])

    useEffect(() => {
        window.addEventListener("resize", onResize);
        return () => {
            window.removeEventListener("resize", onResize);
        }
    }, [onResize])

    useEffect(() => {
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
            }]
        };

        const spline = 'spline'

        const series = {
            playersOnline: hasPermission('page.server.performance.graphs.players.online') ? {
                name: t('html.label.playersOnline'),
                type: 'areaspline',
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.playersOnline,
                color: data.colors.playersOnline,
                yAxis: 0
            } : undefined,
            tps: hasPermission('page.server.performance.graphs.tps') ? {
                name: t('html.label.tps'),
                type: spline,
                color: "var(--color-graphs-tps-high)",
                zones: zones.tps,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.tps,
                yAxis: 1
            } : undefined,
            cpu: hasPermission('page.server.performance.graphs.cpu') ? {
                name: t('html.label.cpu'),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.cpu,
                color: "var(--color-data-performance-cpu)",
                yAxis: 2
            } : undefined,
            ram: hasPermission('page.server.performance.graphs.ram') ? {
                name: t('html.label.ram'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.ram,
                color: "var(--color-data-performance-ram)",
                yAxis: 3
            } : undefined,
            entities: hasPermission('page.server.performance.graphs.entities') ? {
                name: t('html.label.loadedEntities'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.entities,
                color: "var(--color-data-performance-entities)",
                yAxis: 4
            } : undefined,
            chunks: hasPermission('page.server.performance.graphs.chunks') ? {
                name: t('html.label.loadedChunks'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.chunks,
                color: "var(--color-data-performance-chunks)",
                yAxis: 5
            } : undefined,
            mspt: hasPermission('page.server.performance.graphs.mspt') && hasValuesInSeries(dataSeries.mspt95thPercentile) ? {
                name: t('html.label.msptPercentile', {percentile: 95}),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.mspt95thPercentile,
                color: "var(--color-data-performance-mspt-percentile)",
                yAxis: 6
            } : undefined
        };

        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        })
        Highcharts.setOptions(graphTheming);
        setGraph(Highcharts.stockChart(id, {
            chart: {
                noData: t('html.label.noDataToDisplay')
            },
            rangeSelector: {
                selected: 2,
                buttons: translateLinegraphButtons(t)
            },
            xAxis: {
                events: {
                    afterSetExtremes: (event) => {
                        if (onSetExtremes) onSetExtremes(event);
                    }
                }
            },
            yAxis,
            title: {text: ''},
            plotOptions: {
                areaspline: {
                    fillOpacity: nightModeEnabled ? 0.2 : 0.4
                }
            },
            legend: {
                enabled: true
            },
            time: {
                timezoneOffset: timeZoneOffsetMinutes
            },
            series: [series.playersOnline, series.tps, series.mspt, series.cpu, series.ram, series.entities, series.chunks, pluginHistorySeries].filter(s => s)
        }));
    }, [data, dataSeries, graphTheming, nightModeEnabled, id, t, timeZoneOffsetMinutes, pluginHistorySeries])
    useEffect(() => {
        if (graph?.xAxis?.length && extremes) {
            graph.xAxis[0].setExtremes(extremes.min, extremes.max);
        }
    }, [graph, extremes]);
    return (
        <div className="chart-area" style={{height: "450px"}} id={id}>
            <span className="loader"/>
        </div>
    )
};

export default AllPerformanceGraph