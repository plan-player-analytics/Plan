import React, {useEffect} from "react";
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import Highcharts from "highcharts/highstock";
import {linegraphButtons, tooltip} from "../../util/graphs";

const PlayersOnlineGraph = ({data}) => {
    const {t} = useTranslation();
    const {graphTheming} = useTheme();

    useEffect(() => {
        const playersOnlineSeries = {
            name: t('html.label.playersOnline'),
            type: 'areaspline',
            tooltip: tooltip.zeroDecimals,
            data: data.playersOnline,
            color: data.color,
            yAxis: 0
        }
        Highcharts.setOptions(graphTheming);
        Highcharts.stockChart("online-activity-graph", {
            rangeSelector: {
                selected: 2,
                buttons: linegraphButtons
            },
            yAxis: {
                softMax: 2,
                softMin: 0
            },
            title: {text: ''},
            plotOptions: {
                areaspline: {
                    fillOpacity: 0.4
                }
            },
            series: [playersOnlineSeries]
        })
    }, [data, graphTheming, t])

    return (
        <div className="chart-area" id="online-activity-graph">
            <span className="loader"/>
        </div>
    )
}

export default PlayersOnlineGraph