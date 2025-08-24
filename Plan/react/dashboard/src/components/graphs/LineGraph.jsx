import {useTheme} from "../../hooks/themeHook";
import React, {useEffect, useState} from "react";
import {translateLinegraphButtons} from "../../util/graphs";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/no-data-to-display";
import "highcharts/esm/modules/accessibility"
import {useTranslation} from "react-i18next";
import {useMetadata} from "../../hooks/metadataHook";
import {localeService} from "../../service/localeService.js";

const LineGraph = ({
                       id,
                       series,
                       legendEnabled,
                       tall,
                       yAxis,
                       selectedRange,
                       extremes,
                       onSetExtremes,
                       alreadyOffsetTimezone,
                       options
                   }) => {
    const {t} = useTranslation()
    const {graphTheming, nightModeEnabled} = useTheme();
    const {timeZoneOffsetMinutes} = useMetadata();
    const [graph, setGraph] = useState(undefined);

    useEffect(() => {
        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        })
        Highcharts.setOptions(graphTheming);
        setGraph(Highcharts.stockChart(id, options || {
            chart: {
                noData: t('html.label.noDataToDisplay')
            },
            rangeSelector: {
                selected: selectedRange !== undefined ? selectedRange : 2,
                buttons: translateLinegraphButtons(t)
            },
            yAxis: yAxis || {
                softMax: 2,
                softMin: 0
            },
            xAxis: {
                events: {
                    afterSetExtremes: (event) => {
                        if (onSetExtremes) onSetExtremes(event);
                    }
                }
            },
            title: {text: ''},
            plotOptions: {
                areaspline: {
                    fillOpacity: nightModeEnabled ? 0.2 : 0.4
                }
            },
            legend: {
                enabled: legendEnabled
            },
            time: {
                timezoneOffset: alreadyOffsetTimezone ? 0 : timeZoneOffsetMinutes
            },
            series: series
        }));
    }, [options, series, id, t,
        graphTheming, nightModeEnabled, alreadyOffsetTimezone, timeZoneOffsetMinutes,
        legendEnabled, yAxis,
        onSetExtremes, setGraph, selectedRange]);

    useEffect(() => {
        if (graph?.xAxis?.length && extremes) {
            graph.xAxis[0].setExtremes(extremes.min, extremes.max);
        }
    }, [graph, extremes]);

    const style = tall ? {height: "450px"} : undefined;

    return (
        <div className="chart-area" style={style} id={id}>
            <span className="loader"/>
        </div>
    )
}

export default LineGraph