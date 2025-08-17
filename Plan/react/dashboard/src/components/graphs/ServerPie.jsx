import React, {useEffect} from "react";
import Highcharts from 'highcharts';
import "highcharts/modules/no-data-to-display";
import "highcharts/modules/accessibility";

import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import {useTranslation} from "react-i18next";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import {useTimePreferences} from "../text/FormattedTime.jsx";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat.js";
import {localeService} from "../../service/localeService.js";

const ServerPie = ({colors, series}) => {
    const {t} = useTranslation();
    const {nightModeEnabled, graphTheming} = useTheme();
    const {preferencesLoaded} = usePreferences();
    const timePreferences = useTimePreferences();

    useEffect(() => {
        const reduceColors = (colorsToReduce) => colorsToReduce.map(color => withReducedSaturation(color));

        const pieSeries = {
            name: t('html.label.serverPlaytime'),
            colorByPoint: true,
            colors: nightModeEnabled ? reduceColors(colors) : colors,
            data: series
        };

        Highcharts.setOptions(graphTheming);
        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        });
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
                    return '<b>' + this.point.name + ':</b> ' + formatTimeAmount(timePreferences, this.y) + ' (' + this.percentage.toFixed(2) + '%)';
                }
            },
            series: [pieSeries]
        });
    }, [colors, series, graphTheming, nightModeEnabled, t]);

    if (!preferencesLoaded) return <Loader/>;

    return (<div className="chart-pie" id="server-pie"/>);
}

export default ServerPie;