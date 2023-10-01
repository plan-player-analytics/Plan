import React, {useEffect} from "react";
import Highcharts from 'highcharts';

import {formatTimeAmount} from '../../util/formatters'
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import {useTranslation} from "react-i18next";
import NoDataDisplay from "highcharts/modules/no-data-to-display";
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

        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        Highcharts.setOptions(graphTheming);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}});
        Highcharts.chart('server-pie', {
            chart: {
                noData: t('html.label.noDataToDisplay'),
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