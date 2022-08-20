import React, {useEffect} from 'react';

import {linegraphButtons, tooltip} from "../../../util/graphs";
import Highcharts from "highcharts/highstock";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import {withReducedSaturation} from "../../../util/colors";

const PingGraph = ({id, data}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();

    useEffect(() => {
        const spline = 'spline'

        const series = {
            avgPing: {
                name: t('html.label.averagePing'),
                type: spline,
                tooltip: tooltip.twoDecimals,
                data: data.avg_ping_series,
                color: nightModeEnabled ? withReducedSaturation(data.colors.avg) : data.colors.avg,
            },
            maxPing: {
                name: t('html.label.worstPing'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: data.max_ping_series,
                color: nightModeEnabled ? withReducedSaturation(data.colors.max) : data.colors.max,
            },
            minPing: {
                name: t('html.label.bestPing'),
                type: spline,
                tooltip: tooltip.zeroDecimals,
                data: data.min_ping_series,
                color: nightModeEnabled ? withReducedSaturation(data.colors.min) : data.colors.min,
            }
        };

        NoDataDisplay(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);
        Highcharts.stockChart(id, {
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
            series: [series.avgPing, series.maxPing, series.minPing]
        });
    }, [data, graphTheming, nightModeEnabled, id, t])

    return (
        <div className="chart-area" style={{height: "450px"}} id={id}>
            <span className="loader"/>
        </div>
    )
};

export default PingGraph