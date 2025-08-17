import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import React, {useEffect} from "react";
import Highcharts from "highcharts/highcharts";
import "highcharts/modules/no-data-to-display";
import "highcharts/modules/accessibility";
import {localeService} from "../../service/localeService.js";

const FunctionPlotGraph = ({
                               id,
                               series,
                               legendEnabled,
                               tall,
                               yPlotLines,
                               yPlotBands,
                               xPlotLines,
                               xPlotBands,
                               options
                           }) => {
    const {t} = useTranslation()
    const {graphTheming, nightModeEnabled} = useTheme();

    useEffect(() => {
        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        })
        Highcharts.setOptions(graphTheming);
        Highcharts.chart(id, options ? options : {
            chart: {
                noData: t('html.label.noDataToDisplay')
            },
            yAxis: {
                plotLines: yPlotLines,
                plotBands: yPlotBands
            },
            xAxis: {
                softMin: -0.5,
                plotLines: xPlotLines,
                plotBands: xPlotBands,
            },
            title: {text: ''},
            plotOptions: {
                areaspline: {
                    fillOpacity: nightModeEnabled ? 0.2 : 0.4
                }
            },
            legend: {
                enabled: legendEnabled,
            },
            series: series
        });
    }, [options, series, id, t, graphTheming, nightModeEnabled, legendEnabled,
        yPlotLines, yPlotBands, xPlotLines, xPlotBands]);

    const style = tall ? {height: "450px"} : undefined;

    return (
        <div className="chart-area" style={style} id={id}>
            <span className="loader"/>
        </div>
    )
}

export default FunctionPlotGraph