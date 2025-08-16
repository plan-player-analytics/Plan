import React, {useEffect, useState} from "react";
import {useTranslation} from "react-i18next";
import {tooltip} from "../../util/graphs";
import LineGraph from "./LineGraph";
import {ChartLoader} from "../navigation/Loader";

const PlayersOnlineGraph = ({data, selectedRange, extremes, onSetExtremes, color}) => {
    const {t} = useTranslation();
    const [series, setSeries] = useState([]);

    useEffect(() => {
        if (!data) return;
        const playersOnlineSeries = {
            name: t('html.label.playersOnline'),
            type: 'areaspline',
            tooltip: tooltip.zeroDecimals,
            data: data.playersOnline,
            color: color || "var(--color-graphs-players-online)",
            yAxis: 0
        }
        setSeries([playersOnlineSeries]);
    }, [data, t])

    if (!data) return <ChartLoader/>;

    return (
        <LineGraph id="players-online-graph"
                   series={series}
                   selectedRange={selectedRange}
                   extremes={extremes}
                   onSetExtremes={onSetExtremes}/>
    )
}

export default PlayersOnlineGraph