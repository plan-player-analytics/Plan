import React, {useEffect} from 'react';

import {linegraphButtons, tooltip} from "../../../util/graphs";
import Highcharts from "highcharts/highstock";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import Accessibility from "highcharts/modules/accessibility";
import {useMetadata} from "../../../hooks/metadataHook";

const PingGraph = ({id, data}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    const {timeZoneOffsetMinutes} = useMetadata();

    useEffect(() => {
        const spline = 'spline'

        const series = {
            avgPing: {
                name: t('html.label.averagePing'),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: data.avg_ping_series,
                color: "var(--color-graphs-ping-avg)",
            },
            maxPing: {
                name: t('html.label.worstPing'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: data.max_ping_series,
                color: "var(--color-graphs-ping-max)",
            },
            minPing: {
                name: t('html.label.bestPing'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: data.min_ping_series,
                color: "var(--color-graphs-ping-min)",
            }
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
            yAxis: {
                labels: {
                    formatter: function () {
                        return this.value + ' ms'
                    }
                },
                softMin: 0
            },
            title: {text: ''},
            legend: {
                enabled: true
            },
            time: {
                timezoneOffset: timeZoneOffsetMinutes
            },
            series: [series.avgPing, series.maxPing, series.minPing]
        });
    }, [data, graphTheming, nightModeEnabled, id, t, timeZoneOffsetMinutes])

    return (
        <div className="chart-area" style={{height: "450px"}} id={id}>
            <span className="loader"/>
        </div>
    )
};

export default PingGraph