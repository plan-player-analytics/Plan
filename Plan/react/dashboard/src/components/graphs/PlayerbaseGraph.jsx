import {useTranslation} from "react-i18next";
import React, {useEffect, useMemo} from "react";
import {useTheme} from "../../hooks/themeHook";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/no-data-to-display";
import "highcharts/esm/modules/accessibility";
import {nameToCssVariable} from "../../util/colors";
import {useI18nFriendlyLanguage} from "../../service/localeService.js";
import {useDateFormatter} from "../../util/format/useDateFormatter.js";

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
    const {graphTheming} = useTheme();
    const {formatDate} = useDateFormatter(false, {
        pattern: 'MMMM dd',
        recentDaysPattern: 'MMMM dd'
    });

    const id = 'playerbase-graph';

    const graphOptions = useMemo(() => {
        const labels = data?.activity_labels.map(formatDate);
        const series = data?.activity_series.map(dataSet => {
            return {
                ...dataSet,
                name: t(dataSet.name),
                color: activityGroupToColor(dataSet.name)
            }
        });

        return {
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
        };
    }, [t, data, formatDate]);

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
        Highcharts.chart(id, graphOptions)
    }, [graphTheming, id, t, graphOptions])

    return (
        <div className="chart-area" id={id}>
            <span className="loader"/>
        </div>
    )
}

export default PlayerbaseGraph;