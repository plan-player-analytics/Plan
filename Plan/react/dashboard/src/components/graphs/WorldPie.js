import React, {useEffect} from "react";
import Highcharts from 'highcharts';
import factory from 'highcharts/modules/drilldown';

import {formatTimeAmount} from '../../util/formatters'
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";

const WorldPie = ({id, gmColors, worldSeries, gmSeries}) => {
    useEffect(() => {
        factory(Highcharts)
    }, []);

    const {nightModeEnabled, graphTheming} = useTheme();

    useEffect(() => {
        const reduceColors = (series) => {
            return series.map(slice => {
                return {...slice, color: withReducedSaturation(slice.color)};
            })
        }

        const pieSeries = {
            name: 'World Playtime',
            colorByPoint: true,
            data: nightModeEnabled ? reduceColors(worldSeries) : worldSeries
        };

        const defaultTitle = '';
        const defaultSubtitle = 'Click to expand';
        Highcharts.setOptions(graphTheming);
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
                    drillup: function () {
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
    }, [worldSeries, gmSeries, graphTheming, nightModeEnabled, id, gmColors]);

    return (<div className="chart-pie" id={id}/>)
}

export default WorldPie;