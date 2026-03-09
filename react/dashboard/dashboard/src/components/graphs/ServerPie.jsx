import React, {useEffect, useMemo} from "react";
import Highcharts from 'highcharts/esm/highcharts';
import "highcharts/esm/modules/no-data-to-display";
import "highcharts/esm/modules/accessibility";

import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import {useTranslation} from "react-i18next";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import {useTimePreferences} from "../text/FormattedTime.jsx";
import {classNames} from "../../util/classNames.ts";
import {useTimeAmountFormatter} from "../../util/format/useTimeAmountFormatter.js";
import {useI18nFriendlyLanguage} from "../../service/localeService.js";

const ServerPie = ({colors, series}) => {
    const {t} = useTranslation();
    const {nightModeEnabled, graphTheming} = useTheme();
    const {preferencesLoaded} = usePreferences();
    const {formatTime} = useTimeAmountFormatter();
    const timePreferences = useTimePreferences();

    const chart = useMemo(() => {
        const reduceColors = (colorsToReduce) => colorsToReduce.map(color => withReducedSaturation(color));
        const pieSeries = {
            name: t('html.label.serverPlaytime'),
            colorByPoint: true,
            colors: nightModeEnabled ? reduceColors(colors) : colors,
            data: series
        };
        return {
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
                    return '<b>' + this.point.name + ':</b> ' + formatTime(this.y) + ' (' + this.percentage.toFixed(2) + '%)';
                }
            },
            series: [pieSeries]
        }
    }, [series, colors, formatTime, t])
    const locale = useI18nFriendlyLanguage();
    useEffect(() => {
        Highcharts.setOptions({
            lang: {
                locale: locale,
                noData: t('html.label.noDataToDisplay')
            }
        })
    }, [locale]);
    useEffect(() => {
        Highcharts.setOptions(graphTheming);
        Highcharts.chart('server-pie', chart);
    }, [chart, graphTheming]);

    if (!preferencesLoaded) return <Loader/>;

    return (<div className={classNames("chart-pie", series.length > 7 ? 'big' : undefined)} id="server-pie"/>);
}

export default ServerPie;