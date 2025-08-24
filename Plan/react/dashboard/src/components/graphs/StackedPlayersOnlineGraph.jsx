import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {tooltip, translateLinegraphButtons} from "../../util/graphs";
import LineGraph from './LineGraph'
import {ChartLoader} from "../navigation/Loader";
import {useTheme} from "../../hooks/themeHook";

const StackedPlayersOnlineGraph = ({data}) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();
    const [graphOptions, setGraphOptions] = useState({title: {text: ''},});

    useEffect(() => {
        if (!data) return;
        const playersOnlineSeries = data.graphs.map(graph => {
            return {
                name: t('html.label.playersOnline') + ' (' + graph.server.serverName + ')',
                type: 'areaspline',
                tooltip: tooltip.zeroDecimals,
                data: graph.points.map(point => {
                    // Ensure that the points can be stacked by moving data to minute level
                    point[0] -= (point[0] % 60000);
                    return point;
                }),
                color: data.color,
                yAxis: 0
            }
        });
        setGraphOptions({
            title: {text: ''},
            rangeSelector: {
                selected: 2,
                buttons: translateLinegraphButtons(t)
            },
            chart: {
                noData: t('html.label.noDataToDisplay'),
                zooming: {
                    type: 'xy'
                }
            },
            plotOptions: {
                areaspline: {
                    fillOpacity: nightModeEnabled ? 0.2 : 0.4,
                    stacking: 'normal'
                }
            },
            legend: {
                enabled: true,
            },
            xAxis: {
                zoomEnabled: true,
                title: {
                    enabled: false
                }
            },
            yAxis: {
                zoomEnabled: true,
                title: {text: t('html.label.players')},
                softMax: 2,
                min: 0
            },
            series: playersOnlineSeries
        })
    }, [data, nightModeEnabled, t])

    if (!data) return <ChartLoader/>;

    return (
        <LineGraph id="stacked-players-online-graph"
                   options={graphOptions}/>
    )
}

export default StackedPlayersOnlineGraph