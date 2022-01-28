import React, {useEffect} from "react";
import Highcharts from 'highcharts';
import factory from 'highcharts/modules/drilldown';

import {formatTimeAmount} from '../../util/formatters'

// TODO Graphs context for night mode switch

const WorldPie = ({id, gmColors, worldSeries, gmSeries}) => {
    useEffect(() => {
        factory(Highcharts)
    }, [])

    useEffect(() => {
        const pieSeries = {
            name: 'World Playtime',
            colorByPoint: true,
            data: worldSeries
        };

        const defaultTitle = '';
        const defaultSubtitle = 'Click to expand';
        const chart = Highcharts.chart(id, {
            chart: {
                backgroundColor: 'transparent',
                plotBackgroundColor: 'transparent',
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie',
                events: {
                    drilldown: function (e) {
                        chart.setTitle({text: '' + e.point.name}, {text: ''});
                    },
                    drillup: function (e) {
                        chart.setTitle({text: defaultTitle}, {text: defaultSubtitle});
                    }
                }
            },
            title: {text: defaultTitle},
            subtitle: {
                text: defaultSubtitle
            },
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
            series: [pieSeries],
            drilldown: {
                series: gmSeries.map(function (d) {
                    return {name: d.name, id: d.id, colors: gmColors, data: d.data}
                })
            }
        });
    }, [worldSeries, gmSeries]);

    return (<div className="chart-pie" id={id}/>)
}

export default WorldPie;