import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {tooltip} from "../../util/graphs";
import LineGraph from "./LineGraph";

const PlayerPingGraph = ({data}) => {
    const {t} = useTranslation();
    const [series, setSeries] = useState([]);

    useEffect(() => {
        const avgPingSeries = {
            name: t('html.label.averagePing'),
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            data: data.avg_ping_series,
            color: "var(--color-graphs-ping-avg)",
        }
        const maxPingSeries = {
            name: t('html.label.worstPing'),
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            data: data.max_ping_series,
            color: "var(--color-graphs-ping-max)",
        }
        const minPingSeries = {
            name: t('html.label.bestPing'),
            type: 'spline',
            tooltip: tooltip.twoDecimals,
            data: data.min_ping_series,
            color: "var(--color-graphs-ping-min)",
        }
        setSeries([avgPingSeries, maxPingSeries, minPingSeries]);
    }, [data, t])

    return (
        <LineGraph id="ping-graph" series={series}/>
    )
}

export default PlayerPingGraph