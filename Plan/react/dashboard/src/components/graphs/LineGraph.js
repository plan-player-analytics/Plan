import {useTheme} from "../../hooks/themeHook";
import React, {useEffect, useState} from "react";
import {linegraphButtons} from "../../util/graphs";
import Highcharts from "highcharts/highstock";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import Accessibility from "highcharts/modules/accessibility"
import {useTranslation} from "react-i18next";

const LineGraph = ({id, series, legendEnabled, tall, yAxis, selectedRange, extremes, onSetExtremes}) => {
    const {t} = useTranslation()
    const {graphTheming, nightModeEnabled} = useTheme();
    const [graph, setGraph] = useState(undefined);

    useEffect(() => {
        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);
        setGraph(Highcharts.stockChart(id, {
            rangeSelector: {
                selected: selectedRange !== undefined ? selectedRange : 2,
                buttons: linegraphButtons
            },
            yAxis: yAxis || {
                softMax: 2,
                softMin: 0
            },
            xAxis: {
                events: {
                    afterSetExtremes: (event) => onSetExtremes(event)
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
            series: series
        }));
    }, [series, graphTheming, id, t, nightModeEnabled, legendEnabled, yAxis, onSetExtremes, setGraph])
    useEffect(() => {
        if (graph && extremes) {
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