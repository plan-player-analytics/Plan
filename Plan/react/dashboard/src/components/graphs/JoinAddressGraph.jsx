import React, {useEffect} from 'react';
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/no-data-to-display";
import "highcharts/esm/modules/accessibility";
import {translateLinegraphButtons} from "../../util/graphs";
import {localeService} from "../../service/localeService.js";

const JoinAddressGraph = ({id, data, colors, stack}) => {
    const {t} = useTranslation()
    const {nightModeEnabled, graphTheming} = useTheme();

    useEffect(() => {
        const getColor = i => {
            const color = colors[i % colors.length];
            return nightModeEnabled ? withReducedSaturation(color) : color;
        }

        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        })
        Highcharts.setOptions(graphTheming);

        const valuesByAddress = {};
        const dates = []
        for (const point of data || []) {
            dates.push(point.date);
            for (const address of point.joinAddresses) {
                if (!valuesByAddress[address.joinAddress]) valuesByAddress[address.joinAddress] = [];
                valuesByAddress[address.joinAddress].push([point.date, address.count]);
            }
        }

        const labels = dates;
        const series = Object.entries(valuesByAddress).map((entry, i) => {
            let name = entry[0];
            if (name === 'plugin.generic.unknown') name = t('plugin.generic.unknown')
            if (i >= colors.length) return {name: name, data: entry[1]};
            return {name: name, data: entry[1], color: getColor(i)};
        });

        Highcharts.stockChart(id, {
            chart: {
                noData: t('html.label.noDataToDisplay'),
                type: "column"
            },
            rangeSelector: {
                selected: 3,
                buttons: translateLinegraphButtons(t)
            },
            xAxis: {
                categories: labels,
                tickmarkPlacement: 'on',
                title: {
                    enabled: false
                },
                ordinal: false
            },
            yAxis: {
                softMax: 2,
                softMin: 0
            },
            title: {text: ''},
            plotOptions: {
                column: {
                    stacking: stack ? 'normal' : undefined,
                    lineWidth: 1
                }
            },
            legend: {
                enabled: true
            },
            series: series
        })
    }, [data, colors, graphTheming, id, t, nightModeEnabled, stack])

    return (
        <div className="chart-area" style={{height: "450px"}} id={id}>
            <span className="loader"/>
        </div>
    )
};

export default JoinAddressGraph