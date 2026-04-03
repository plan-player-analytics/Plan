import React, {useEffect, useMemo, useRef} from "react";
import Highcharts from 'highcharts/esm/highcharts';
import "highcharts/esm/modules/accessibility";
import 'highcharts/esm/modules/drilldown';
import "highcharts/esm/modules/no-data-to-display"

import {useTheme} from "../../hooks/themeHook";
import {nameToCssVariable, withReducedSaturation} from "../../util/colors";
import {useTranslation} from "react-i18next";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import Loader from "../navigation/Loader.tsx";
import {useThemeStorage} from "../../hooks/context/themeContextHook.tsx";
import {classNames} from "../../util/classNames.ts";
import {useTimeAmountFormatter} from "../../util/format/useTimeAmountFormatter.js";

const WorldPie = ({id, worldSeries, gmSeries}) => {
    const {t} = useTranslation();
    const {preferencesLoaded} = usePreferences();
    const {formatTime} = useTimeAmountFormatter();

    const {nightModeEnabled, graphTheming} = useTheme();
    const {usedUseCases} = useThemeStorage();

    const gmPieColors = usedUseCases?.graphs?.pie?.drilldown?.map(nameToCssVariable) || [];

    const chartRef = useRef(undefined);
    const chart = useMemo(() => {
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
        return {
            chart: {
                noData: t('html.label.noDataToDisplay'),
                backgroundColor: 'transparent',
                plotBackgroundColor: 'transparent',
                plotBorderWidth: null,
                plotShadow: false,
                type: 'pie',
                events: {
                    drilldown: function (e) {
                        chartRef.current.setTitle({text: '' + e.point.name}, {text: ''});
                    },
                    drillup: function () {
                        chartRef.current.setTitle({text: defaultTitle}, {text: defaultSubtitle});
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
                    return '<b>' + this.point.name + ':</b> ' + formatTime(this.y) + ' (' + this.percentage.toFixed(2) + '%)';
                }
            },
            series: [pieSeries],
            drilldown: {
                series: gmSeries.map(function (d) {
                    return {name: d.name, id: d.id, colors: gmPieColors, data: d.data}
                })
            }
        };
    }, [worldSeries, gmSeries, t, formatTime]);

    useEffect(() => {
        Highcharts.setOptions(graphTheming);
        const timeout = setTimeout(() => {
            chartRef.current = Highcharts.chart(id, chart);
        }, 25); // Timeout to allow collapse to open
        return () => {
            clearTimeout(timeout);
        }
    }, [graphTheming, chart]);

    if (!preferencesLoaded) return <Loader/>;

    return (<div className={classNames("chart-pie", worldSeries.length > 7 ? 'big' : undefined)} id={id}/>)
}

export default WorldPie;