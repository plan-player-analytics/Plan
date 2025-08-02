import React, {useEffect, useState} from 'react';

import {linegraphButtons, tooltip} from "../../../util/graphs";
import Highcharts from "highcharts/highstock";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import Accessibility from "highcharts/modules/accessibility";
import {useMetadata} from "../../../hooks/metadataHook";
import {useGraphExtremesContext} from "../../../hooks/interaction/graphExtremesContextHook.jsx";

const DiskPerformanceGraph = ({id, data, dataSeries, pluginHistorySeries}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    const {timeZoneOffsetMinutes} = useMetadata();
    const {extremes, onSetExtremes} = useGraphExtremesContext();
    const [graph, setGraph] = useState(undefined);
    useEffect(() => {
        const zones = {
            disk: [{
                value: data.zones.diskThresholdMed,
                color: "var(--color-graphs-disk-low)"
            }, {
                value: data.zones.diskThresholdHigh,
                color: "var(--color-graphs-disk-medium)"
            }, {
                value: Number.MAX_VALUE,
                color: "var(--color-graphs-disk-high)"
            }]
        };

        const series = {
            disk: {
                name: t('html.label.disk'),
                type: 'areaspline',
                color: "var(--color-graphs-disk-high)",
                zones: zones.disk,
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.disk
            }
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
                selected: 2,
                buttons: linegraphButtons
            }, xAxis: {
                events: {
                    afterSetExtremes: (event) => {
                        if (onSetExtremes) onSetExtremes(event);
                    }
                }
            },
            yAxis: {
                labels: {
                    formatter: function () {
                        return this.value + ' MB';
                    }
                },
                softMin: 0
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
            series: [series.disk, pluginHistorySeries].filter(s => s)
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

export default DiskPerformanceGraph