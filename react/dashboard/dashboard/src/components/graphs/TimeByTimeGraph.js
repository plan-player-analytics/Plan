import {useTranslation} from "react-i18next";
import React, {useEffect, useState} from "react";
import {tooltip} from "../../util/graphs";
import LineGraph from "./LineGraph";

const TimeByTimeGraph = ({data}) => {
    const {t} = useTranslation();
    const [series, setSeries] = useState([]);

    useEffect(() => {
        const uniquePlayers = {
            name: t('html.label.uniquePlayers'),
            type: 'spline',
            tooltip: tooltip.zeroDecimals,
            data: data.uniquePlayers,
            color: data.colors.playersOnline
        };
        const newPlayers = {
            name: t('html.label.newPlayers'),
            type: 'spline',
            tooltip: tooltip.zeroDecimals,
            data: data.newPlayers,
            color: data.colors.newPlayers
        };
        setSeries([uniquePlayers, newPlayers]);
    }, [data, t])

    return (
        <LineGraph id="day-by-day-graph" series={series}/>
    )
}

export default TimeByTimeGraph