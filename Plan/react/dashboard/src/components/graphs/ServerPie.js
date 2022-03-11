import React, {useEffect} from "react";
import Highcharts from 'highcharts';

import {formatTimeAmount} from '../../util/formatters'
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";

const ServerPie = ({colors, series}) => {
    const {nightModeEnabled, graphTheming} = useTheme();

    useEffect(() => {
        const reduceColors = (colorsToReduce) => colorsToReduce.map(color => withReducedSaturation(color));

        const pieSeries = {
            name: 'Server Playtime',
            colorByPoint: true,
            colors: nightModeEnabled ? reduceColors(colors) : colors,
            data: series
        };

        Highcharts.setOptions(graphTheming);
        Highcharts.chart('server-pie', {
            chart: {
                backgroundColor: 'transparent',
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie'
            },
            title: {text: ''},
            plotOptions: {
                pie: {
                    allowPointSelect: true,
                    cursor: 'pointer',
                    dataLabels: {
                        enabled: false
                    },
                    showInLegend: true
                }
            },
            tooltip: {
                formatter: function () {
                    return '<b>' + this.point.name + ':</b> ' + formatTimeAmount(this.y) + ' (' + this.percentage.toFixed(2) + '%)';
                }
            },
            series: [pieSeries]
        });
    }, [colors, series, graphTheming, nightModeEnabled]);

    return (<div className="chart-pie" id="server-pie"/>);
}

export default ServerPie;