import React, {useEffect, useState} from 'react';

import {linegraphButtons, tooltip} from "../../../util/graphs";
import Highcharts from "highcharts/highstock";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import Accessibility from "highcharts/modules/accessibility";
import {useMetadata} from "../../../hooks/metadataHook";
import {useAuth} from "../../../hooks/authenticationHook.jsx";
import {useGraphExtremesContext} from "../../../hooks/interaction/graphExtremesContextHook.jsx";

const CpuRamPerformanceGraph = ({id, data, dataSeries, pluginHistorySeries}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    const {timeZoneOffsetMinutes} = useMetadata();
    const {hasPermission} = useAuth();
    const {extremes, onSetExtremes} = useGraphExtremesContext();
    const [graph, setGraph] = useState(undefined);

    useEffect(() => {
        const spline = 'spline'

        const series = {
            playersOnline: hasPermission('page.server.performance.graphs.players.online') ? {
                name: t('html.label.playersOnline'),
                type: 'areaspline',
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.playersOnline,
                color: "var(--color-graphs-players-online)",
                yAxis: 0
            } : {},
            cpu: hasPermission('page.server.performance.graphs.cpu') ? {
                name: t('html.label.cpu'),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.cpu,
                color: "var(--color-graphs-cpu)",
                yAxis: 1
            } : {},
            ram: hasPermission('page.server.performance.graphs.ram') ? {
                name: t('html.label.ram'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.ram,
                color: "var(--color-graphs-ram)",
                yAxis: 2
            } : {}
        };

        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);
        setGraph(Highcharts.stockChart(id, {
            chart: {
                noData: t('html.label.noDataToDisplay')
            },
            rangeSelector: {
                selected: 1,
                buttons: linegraphButtons
            },
            yAxis: [{
                labels: {
                    formatter: function () {
                        return this.value + ' ' + t('html.unit.players')
                    }
                },
                softMin: 0,
                softMax: 2
            }, {
                labels: {
                    formatter: function () {
                        return this.value + ' %'
                    }
                },
                softMin: 0,
                softMax: 100
            }, {
                labels: {
                    formatter: function () {
                        return this.value + ' MB'
                    }
                },
                softMin: 0
            }], xAxis: {
                events: {
                    afterSetExtremes: (event) => {
                        if (onSetExtremes) onSetExtremes(event);
                    }
                }
            },
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
            series: [series.playersOnline, series.cpu, series.ram, pluginHistorySeries]
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

export default CpuRamPerformanceGraph