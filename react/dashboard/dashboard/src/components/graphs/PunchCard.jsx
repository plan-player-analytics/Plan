import React, {useEffect} from "react";
import Highcharts from 'highcharts/esm/highcharts';
import "highcharts/esm/modules/accessibility";
import "highcharts/esm/modules/no-data-to-display"
import {useTheme} from "../../hooks/themeHook";
import {useTranslation} from "react-i18next";
import {localeService} from "../../service/localeService.js";

const PunchCard = ({series}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    useEffect(() => {
        const punchCard = {
            name: t('html.label.relativeJoinActivity'),
            color: "var(--color-graphs-punch-card)",
            type: 'scatter',
            data: series
        };

        Highcharts.setOptions({lang: {locale: localeService.getIntlFriendlyLocale()}})
        Highcharts.setOptions(graphTheming);
        setTimeout(() => Highcharts.chart('punchcard', {
            chart: {
                noData: t('html.label.noDataToDisplay'),
                backgroundColor: 'transparent',
                plotBackgroundColor: 'transparent',
                defaultSeriesType: 'scatter'
            },
            title: {text: ''},
            xAxis: {
                type: 'datetime',
                dateTimeLabelFormats: {
                    // https://www.php.net/manual/en/function.strftime.php
                    hour: '%I %P',
                    day: '%I %P'
                },
                tickInterval: 3600000
            },
            time: {
                timezoneOffset: 0
            },
            yAxis: {
                title: {
                    text: t('html.label.dayOfweek')
                },
                reversed: true,
                categories: t('html.label.weekdays').replaceAll("'", '').split(', ')
            },
            tooltip: {
                pointFormat: t('html.label.active') + ': {point.z}'
            },
            series: [punchCard]
        }), 25)
    }, [series, graphTheming, t, nightModeEnabled])

    return (
        <div className="chart-area" id="punchcard">
            <span className="loader"/>
        </div>
    )
}

export default PunchCard