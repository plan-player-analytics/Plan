import {useTranslation} from "react-i18next";
import React, {useEffect, useState} from "react";
import {tooltip} from "../../util/graphs";
import LineGraph from "./LineGraph";

const TimeByTimeGraph = ({id, data}) => {
    const {t} = useTranslation();
    const [series, setSeries] = useState([]);

    useEffect(() => {
        const uniquePlayers = {
            name: t('html.label.uniquePlayers'),
            type: 'spline',
            tooltip: tooltip.zeroDecimals,
            data: data.uniquePlayers,
            color: "var(--color-data-players-unique)"
        };
        const newPlayers = {
            name: t('html.label.newPlayers'),
            type: 'spline',
            tooltip: tooltip.zeroDecimals,
            data: data.newPlayers,
            color: "var(--color-data-players-new)"
        };
        setSeries([uniquePlayers, newPlayers]);
    }, [data, t])

    return (
        <LineGraph id={id} series={series} alreadyOffsetTimezone/>
    )
}

export default TimeByTimeGraph