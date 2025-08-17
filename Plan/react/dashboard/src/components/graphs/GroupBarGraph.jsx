import React, {useEffect} from 'react';
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import Highcharts from "highcharts/esm/highcharts";
import "highcharts/esm/modules/accessibility";
import "highcharts/esm/modules/no-data-to-display"

const GroupBarGraph = ({id, groups, colors, horizontal, name}) => {
    const {t} = useTranslation();
    const {nightModeEnabled, graphTheming} = useTheme();

    useEffect(() => {
        const reduceColors = (colorsToReduce) => colorsToReduce.map(color => withReducedSaturation(color));

        function getColors() {
            const actualColors = colors ? colors : groups.map(group => group.color);
            return nightModeEnabled ? reduceColors(actualColors) : actualColors;
        }

        const bars = groups.map(group => group.y);
        const categories = groups.map(group => t(group.name));
        const barSeries = {
            name: name,
            colorByPoint: true,
            data: bars,
            colors: getColors()
        };

        Highcharts.setOptions(graphTheming);
        Highcharts.chart(id, {
            chart: {
                noData: t('html.label.noDataToDisplay'),
                type: horizontal ? 'bar' : 'column'
            },
            title: {text: ''},
            xAxis: {
                categories: categories,
                title: {text: ''}
            },
            yAxis: {
                min: 0,
                title: {text: '', align: 'high'},
                labels: {overflow: 'justify'}
            },
            legend: {enabled: false},
            plotOptions: {
                bar: {
                    dataLabels: {enabled: true}
                }
            },
            series: [barSeries]
        })
    }, [id, groups, colors, horizontal, name, graphTheming, nightModeEnabled, t]);

    return (<div id={id} className="chart-area"/>);
};

export default GroupBarGraph