import React, {useEffect} from "react";
import Highcharts from 'highcharts/esm/highcharts';
import "highcharts/esm/modules/accessibility";
import 'highcharts/esm/modules/drilldown';
import "highcharts/esm/modules/no-data-to-display"

import {useTheme} from "../../hooks/themeHook";
import {nameToCssVariable, withReducedSaturation} from "../../util/colors";
import {useTranslation} from "react-i18next";
import {useTimePreferences} from "../text/FormattedTime.jsx";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import Loader from "../navigation/Loader.jsx";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat.js";
import {useThemeStorage} from "../../hooks/context/themeContextHook.tsx";

const WorldPie = ({id, worldSeries, gmSeries}) => {
    const {t} = useTranslation();
    const {preferencesLoaded} = usePreferences();
    const timePreferences = useTimePreferences();

    const {nightModeEnabled, graphTheming} = useTheme();
    const {usedUseCases} = useThemeStorage();

    const gmPieColors = usedUseCases?.graphs?.pie?.drilldown?.map(nameToCssVariable) || [];

    useEffect(() => {
        const reduceColors = (series) => {
            return series.map(slice => {
                return {...slice, color: withReducedSaturation(slice.color)};
            })
        }

        const pieSeries = {
            name: t('html.label.worldPlaytime'),
            colorByPoint: true,
            data: nightModeEnabled ? reduceColors(worldSeries) : worldSeries
        };

        const defaultTitle = '';
        const defaultSubtitle = t('html.text.clickToExpand');
        Highcharts.setOptions(graphTheming);
        setTimeout(() => {
            const chart = Highcharts.chart(id, {
                chart: {
                    noData: t('html.label.noDataToDisplay'),
                    backgroundColor: 'transparent',
                    plotBackgroundColor: 'transparent',
                    plotBorderWidth: null,
                    plotShadow: false,
                    type: 'pie',
                    events: {
                        drilldown: function (e) {
                            chart.setTitle({text: '' + e.point.name}, {text: ''});
                        },
                        drillup: function () {
                            chart.setTitle({text: defaultTitle}, {text: defaultSubtitle});
                        }
                    }
                },
                title: {text: defaultTitle},
                subtitle: {
                    text: defaultSubtitle
                },
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
                series: [pieSeries],
                drilldown: {
                    series: gmSeries.map(function (d) {
                        return {name: d.name, id: d.id, colors: gmPieColors, data: d.data}
                    })
                }
            });
        }, 25)
    }, [worldSeries, gmSeries, graphTheming, nightModeEnabled, id, gmPieColors, t]);

    if (!preferencesLoaded) return <Loader/>;

    return (<div className="chart-pie" id={id}/>)
}

export default WorldPie;