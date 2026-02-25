import React, {useEffect, useState} from 'react';

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

const TpsPerformanceGraph = ({id, data, dataSeries, pluginHistorySeries}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    const {timeZoneOffsetMinutes} = useMetadata();
    const {hasPermission} = useAuth();
    const {extremes, onSetExtremes} = useGraphExtremesContext();
    const [graph, setGraph] = useState(undefined);

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
                color: "var(--color-graphs-players-online)",
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
            msptAverage: hasPermission('page.server.performance.graphs.mspt') && hasValuesInSeries(dataSeries.msptAverage) ? {
                name: t('html.label.msptAverage'),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.msptAverage,
                color: "var(--color-data-performance-mspt-average)",
                yAxis: 2
            } : undefined,
            mspt: hasPermission('page.server.performance.graphs.mspt') && hasValuesInSeries(dataSeries.mspt95thPercentile) ? {
                name: t('html.label.msptPercentile', {percentile: 95}),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.mspt95thPercentile,
                color: "var(--color-data-performance-mspt-percentile)",
                yAxis: 2
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
                selected: 1,
                buttons: translateLinegraphButtons(t)
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
                        return this.value + ' ' + t('html.label.tps')
                    }
                }
            }, {
                opposite: true,
                labels: {
                    formatter: function () {
                        return localeService.localizePing(this.value);
                    }
                },
                softMin: 0,
                softMax: 2
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
            series: [series.playersOnline, series.tps, series.msptAverage, series.mspt, pluginHistorySeries].filter(s => s)
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

export default TpsPerformanceGraph