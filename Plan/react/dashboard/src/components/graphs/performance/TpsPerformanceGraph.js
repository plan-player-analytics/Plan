import React, {useEffect} from 'react';

import {linegraphButtons, tooltip} from "../../../util/graphs";
import Highcharts from "highcharts/highstock";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import {useTranslation} from "react-i18next";
import {useTheme} from "../../../hooks/themeHook";
import {withReducedSaturation} from "../../../util/colors";
import Accessibility from "highcharts/modules/accessibility";

const TpsPerformanceGraph = ({id, data, dataSeries}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();

    useEffect(() => {
        const zones = {
            tps: [{
                value: data.zones.tpsThresholdMed,
                color: nightModeEnabled ? withReducedSaturation(data.colors.low) : data.colors.low
            }, {
                value: data.zones.tpsThresholdHigh,
                color: nightModeEnabled ? withReducedSaturation(data.colors.med) : data.colors.med
            }, {
                value: 30,
                color: nightModeEnabled ? withReducedSaturation(data.colors.high) : data.colors.high
            }]
        };

        const spline = 'spline'
        const series = {
            playersOnline: {
                name: t('html.label.playersOnline'),
                type: 'areaspline',
                tooltip: tooltip.zeroDecimals,
                data: dataSeries.playersOnline,
                color: nightModeEnabled ? withReducedSaturation(data.colors.playersOnline) : data.colors.playersOnline,
                yAxis: 0
            }, tps: {
                name: t('html.label.tps'),
                type: spline,
                color: nightModeEnabled ? withReducedSaturation(data.colors.high) : data.colors.high,
                zones: zones.tps,
                tooltip: tooltip.twoDecimals,
                data: dataSeries.tps,
                yAxis: 1
            }
        };

        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);
        Highcharts.stockChart(id, {
            rangeSelector: {
                selected: 1,
                buttons: linegraphButtons
            },
            yAxis: [{
                labels: {
                    formatter: function () {
                        return this.value + ' ' + t('html.unit.players')
                    }
                },
                softMin: 0,
                softMax: 2
            }, {
                labels: {
                    formatter: function () {
                        return this.value + ' ' + t('html.label.tps')
                    }
                }
            }],
            title: {text: ''},
            plotOptions: {
                areaspline: {
                    fillOpacity: 0.4
                }
            },
            legend: {
                enabled: true
            },
            series: [series.playersOnline, series.tps]
        });
    }, [data, dataSeries, graphTheming, nightModeEnabled, id, t])

    return (
        <div className="chart-area" style={{height: "450px"}} id={id}>
            <span className="loader"/>
        </div>
    )
};

export default TpsPerformanceGraph