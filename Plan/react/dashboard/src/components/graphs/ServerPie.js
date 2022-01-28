import React, {useEffect} from "react";
import Highcharts from 'highcharts';

import {formatTimeAmount} from '../../util/formatters'

// TODO Graphs context for night mode switch

const ServerPie = ({colors, series}) => {
    useEffect(() => {
        const pieSeries = {
            name: 'Server Playtime',
            colorByPoint: true,
            colors: colors,
            data: series
        };

        const chart = Highcharts.chart('server-pie', {
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
    }, [colors, series]);

    return (<div className="chart-pie" id="server-pie"/>);
}

export default ServerPie;