import {useTranslation} from "react-i18next";
import React, {useEffect} from "react";
import {useTheme} from "../../hooks/themeHook";
import NoDataDisplay from "highcharts/modules/no-data-to-display";
import Highcharts from "highcharts/highstock";
import {withReducedSaturation} from "../../util/colors";
import Accessibility from "highcharts/modules/accessibility";

const PlayerbaseGraph = ({data}) => {
    console.log(data);
    const {t} = useTranslation()
    const {nightModeEnabled, graphTheming} = useTheme();

    const id = 'playerbase-graph';

    useEffect(() => {
        const reduceColors = (lines) => lines.map(line => {
            return {...line, color: withReducedSaturation(line.color)}
        });

        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);

        const labels = data?.activity_labels;
        const series = data?.activity_series;

        Highcharts.chart(id, {
            chart: {
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
    }, [data, graphTheming, id, t, nightModeEnabled])

    return (
        <div className="chart-area" id={id}>
            <span className="loader"/>
        </div>
    )
}

export default PlayerbaseGraph;