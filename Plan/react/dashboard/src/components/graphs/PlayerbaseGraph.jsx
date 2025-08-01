import {useTranslation} from "react-i18next";
import React, {useEffect} from "react";
import {useTheme} from "../../hooks/themeHook";
import NoDataDisplay from "highcharts/modules/no-data-to-display";
import Highcharts from "highcharts/highstock";
import {withReducedSaturation} from "../../util/colors";
import Accessibility from "highcharts/modules/accessibility";
import {formatDateWithPreferences, useDatePreferences} from "../text/FormattedDate.jsx";

export const getTranslateLabelForActivityGroup = value => {
    switch (value) {
        case "Very Active":
            return 'html.label.veryActive'
        case "Active":
            return 'html.label.active'
        case "Regular":
            return 'html.label.indexRegular'
        case "Irregular":
            return 'html.label.irregular'
        case "Inactive":
            return 'html.label.indexInactive'
        default:
            return 'plugin.generic.unknown'
    }
}

const PlayerbaseGraph = ({data}) => {
    const {t} = useTranslation()
    const {nightModeEnabled, graphTheming} = useTheme();
    const datePreferences = useDatePreferences();

    const id = 'playerbase-graph';

    useEffect(() => {
        const reduceColors = (lines) => lines.map(line => {
            return {...line, color: withReducedSaturation(line.color)}
        });

        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);

        const labels = data?.activity_labels.map(date => formatDateWithPreferences({
            ...datePreferences,
            pattern: 'MMMM dd',
            recentDaysPattern: 'MMMM dd'
        }, date));
        const series = data?.activity_series.map(dataSet => {
            return {
                ...dataSet,
                name: t(dataSet.name)
            }
        });

        Highcharts.chart(id, {
            chart: {
                noData: t('html.label.noDataToDisplay'),
                type: "area"
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
                area: {
                    stacking: 'normal',
                    lineWidth: 1
                }
            },
            series: nightModeEnabled ? reduceColors(series) : series
        })
    }, [data, graphTheming, id, t, nightModeEnabled, datePreferences])

    return (
        <div className="chart-area" id={id}>
            <span className="loader"/>
        </div>
    )
}

export default PlayerbaseGraph;