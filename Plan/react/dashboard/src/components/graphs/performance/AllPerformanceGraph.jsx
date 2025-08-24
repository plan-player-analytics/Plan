import React, {useCallback, useEffect, useState} from 'react';

import {tooltip, translateLinegraphButtons} from "../../../util/graphs";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/no-data-to-display"
import "highcharts/esm/modules/accessibility";
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import {withReducedSaturation} from "../../../util/colors";
import {useMetadata} from "../../../hooks/metadataHook";
import {useAuth} from "../../../hooks/authenticationHook.jsx";
import {useGraphExtremesContext} from "../../../hooks/interaction/graphExtremesContextHook.jsx";
import {localeService} from "../../../service/localeService.js";

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
                return this.value + ' TPS';
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
    }
]

const AllPerformanceGraph = ({id, data, dataSeries, pluginHistorySeries}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    const {timeZoneOffsetMinutes} = useMetadata();
    const {hasPermission} = useAuth();
    const {extremes, onSetExtremes} = useGraphExtremesContext();
    const [graph, setGraph] = useState(undefined);

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
                color: nightModeEnabled ? withReducedSaturation(data.colors.low) : data.colors.low
            }, {
                value: data.zones.tpsThresholdHigh,
                color: nightModeEnabled ? withReducedSaturation(data.colors.med) : data.colors.med
            }, {
                value: 30,
                color: nightModeEnabled ? withReducedSaturation(data.colors.high) : data.colors.high
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
            } : {},
            tps: hasPermission('page.server.performance.graphs.tps') ? {
                name: t('html.label.tps'),
                type: spline,
                color: nightModeEnabled ? withReducedSaturation(data.colors.high) : data.colors.high,
                zones: zones.tps,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.tps,
                yAxis: 1
            } : {},
            cpu: hasPermission('page.server.performance.graphs.cpu') ? {
                name: t('html.label.cpu'),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.cpu,
                color: nightModeEnabled ? withReducedSaturation(data.colors.cpu) : data.colors.cpu,
                yAxis: 2
            } : {},
            ram: hasPermission('page.server.performance.graphs.ram') ? {
                name: t('html.label.ram'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.ram,
                color: nightModeEnabled ? withReducedSaturation(data.colors.ram) : data.colors.ram,
                yAxis: 3
            } : {},
            entities: hasPermission('page.server.performance.graphs.entities') ? {
                name: t('html.label.loadedEntities'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.entities,
                color: nightModeEnabled ? withReducedSaturation(data.colors.entities) : data.colors.entities,
                yAxis: 4
            } : {},
            chunks: hasPermission('page.server.performance.graphs.chunks') ? {
                name: t('html.label.loadedChunks'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.chunks,
                color: nightModeEnabled ? withReducedSaturation(data.colors.chunks) : data.colors.chunks,
                yAxis: 5
            } : {}
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
            series: [series.playersOnline, series.tps, series.cpu, series.ram, series.entities, series.chunks, pluginHistorySeries].filter(s => s)
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