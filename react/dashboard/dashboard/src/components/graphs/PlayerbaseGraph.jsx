import {useTranslation} from "react-i18next";
import React, {useEffect} from "react";
import {useTheme} from "../../hooks/themeHook";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/no-data-to-display";
import "highcharts/esm/modules/accessibility";
import {nameToCssVariable} from "../../util/colors";
import {formatDateWithPreferences, useDatePreferences} from "../text/FormattedDate.jsx";
import {localeService} from "../../service/localeService.js";

export const activityGroupToColor = label => {
    switch (label) {
        case 'html.label.veryActive':
            return nameToCssVariable('data-players-very-active');
        case 'html.label.active':
            return nameToCssVariable('data-players-active');
        case 'html.label.indexRegular':
            return nameToCssVariable('data-players-regular');
        case 'html.label.irregular':
            return nameToCssVariable('data-players-irregular');
        case 'html.label.indexInactive':
            return nameToCssVariable('data-players-inactive');
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
        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        })
        Highcharts.setOptions(graphTheming);

        const labels = data?.activity_labels.map(date => formatDateWithPreferences({
            ...datePreferences,
            pattern: 'MMMM dd',
            recentDaysPattern: 'MMMM dd'
        }, date));
        const series = data?.activity_series.map(dataSet => {
            return {
                ...dataSet,
                name: t(dataSet.name),
                color: activityGroupToColor(dataSet.name)
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
            series: series
        })
    }, [data, graphTheming, id, t, nightModeEnabled, datePreferences])

    return (
        <div className="chart-area" id={id}>
            <span className="loader"/>
        </div>
    )
}

export default PlayerbaseGraph;