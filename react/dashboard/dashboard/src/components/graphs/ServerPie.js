import React, {useEffect} from "react";
import Highcharts from 'highcharts';

import {formatTimeAmount} from '../../util/formatters'
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import {useTranslation} from "react-i18next";
import Accessibility from "highcharts/modules/accessibility";

const ServerPie = ({colors, series}) => {
    const {t} = useTranslation();
    const {nightModeEnabled, graphTheming} = useTheme();

    useEffect(() => {
        const reduceColors = (colorsToReduce) => colorsToReduce.map(color => withReducedSaturation(color));

        const pieSeries = {
            name: t('html.label.serverPlaytime'),
            colorByPoint: true,
            colors: nightModeEnabled ? reduceColors(colors) : colors,
            data: series
        };

        Accessibility(Highcharts);
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
    }, [colors, series, graphTheming, nightModeEnabled, t]);

    return (<div className="chart-pie" id="server-pie"/>);
}

export default ServerPie;