import React, {useEffect} from 'react';
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import Highcharts from "highcharts";
import Accessibility from "highcharts/modules/accessibility";
import {localeService, reverseRegionLookupMap} from "../../service/localeService.js";

const GeolocationBarGraph = ({series}) => {
    const {t} = useTranslation();
    const {nightModeEnabled, graphTheming} = useTheme();

    useEffect(() => {
        const regions = new Intl.DisplayNames([localeService.getIntlFriendlyLocale()], {type: 'region'});
        const bars = series.map(bar => bar.value);
        const categories = series.map(bar => {
            const code = reverseRegionLookupMap[bar.label];
            return code ? regions.of(code) : bar.label.replace('Local Machine', t('html.value.localMachine'));
        });
        const geolocationBarSeries = {
            color: "var(--color-graphs-world-map-bars)",
            name: t('html.label.players'),
            data: bars
        };

        Accessibility(Highcharts);
        Highcharts.setOptions(graphTheming);
        Highcharts.chart("countryBarChart", {
            chart: {
                noData: t('html.label.noDataToDisplay'),
                type: 'bar'
            },
            title: {text: ''},
            xAxis: {
                categories: categories,
                title: {text: ''}
            },
            yAxis: {
                min: 0,
                title: {text: t('html.label.players'), align: 'high'},
                labels: {overflow: 'justify'}
            },
            legend: {enabled: false},
            plotOptions: {
                bar: {
                    dataLabels: {enabled: true}
                }
            },
            series: [geolocationBarSeries]
        })
    }, [series, graphTheming, nightModeEnabled, t, localeService.clientLocale]);

    return (<div id="countryBarChart"/>);
};

export default GeolocationBarGraph