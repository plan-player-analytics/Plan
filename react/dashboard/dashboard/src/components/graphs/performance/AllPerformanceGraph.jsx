import React, {useCallback, useEffect} from 'react';

import {linegraphButtons, tooltip} from "../../../util/graphs";
import Highcharts from "highcharts/highstock";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import {withReducedSaturation} from "../../../util/colors";
import Accessibility from "highcharts/modules/accessibility";
import {useMetadata} from "../../../hooks/metadataHook";
import {useAuth} from "../../../hooks/authenticationHook.jsx";

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

        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);
        Highcharts.stockChart(id, {
            chart: {
                noData: t('html.label.noDataToDisplay')
            },
            rangeSelector: {
                selected: 2,
                buttons: linegraphButtons
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
            series: [series.playersOnline, series.tps, series.cpu, series.ram, series.entities, series.chunks, pluginHistorySeries]
        });
    }, [data, dataSeries, graphTheming, nightModeEnabled, id, t, timeZoneOffsetMinutes, pluginHistorySeries])

    return (
        <div className="chart-area" style={{height: "450px"}} id={id}>
            <span className="loader"/>
        </div>
    )
};

export default AllPerformanceGraph