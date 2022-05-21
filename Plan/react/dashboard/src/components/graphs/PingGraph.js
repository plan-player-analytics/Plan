import React, {useEffect} from "react";
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import Highcharts from "highcharts/highstock";
import {linegraphButtons, tooltip} from "../../util/graphs";

const PingGraph = ({data}) => {
    const {t} = useTranslation();
    const {graphTheming} = useTheme();

    useEffect(() => {
        const avgPingSeries = {
            name: t('html.label.averagePing'),
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            data: data.avg_ping_series,
            color: data.colors.avg
        }
        const maxPingSeries = {
            name: t('html.label.worstPing'),
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            data: data.max_ping_series,
            color: data.colors.max
        }
        const minPingSeries = {
            name: t('html.label.bestPing'),
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            data: data.min_ping_series,
            color: data.colors.min
        }
        Highcharts.setOptions(graphTheming);
        Highcharts.stockChart("ping-graph", {
            rangeSelector: {
                selected: 2,
                buttons: linegraphButtons
            },
            yAxis: {
                softMax: 2,
                softMin: 0
            },
            title: {text: ''},
            series: [avgPingSeries, maxPingSeries, minPingSeries]
        })
    }, [data, graphTheming, t])

    return (
        <div className="chart-area" id="ping-graph">
            <span className="loader"/>
        </div>
    )
}

export default PingGraph