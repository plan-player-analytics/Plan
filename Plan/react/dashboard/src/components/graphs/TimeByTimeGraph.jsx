import {useTranslation} from "react-i18next";
import React, {useEffect, useState} from "react";
import {tooltip} from "../../util/graphs";
import LineGraph from "./LineGraph";
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";

const TimeByTimeGraph = ({id, data}) => {
    const {t} = useTranslation();
    const [series, setSeries] = useState([]);
    const {nightModeEnabled} = useTheme();

    useEffect(() => {
        const uniquePlayers = {
            name: t('html.label.uniquePlayers'),
            type: 'spline',
            tooltip: tooltip.zeroDecimals,
            data: data.uniquePlayers,
            color: nightModeEnabled ? withReducedSaturation(data.colors.playersOnline) : data.colors.playersOnline
        };
        const newPlayers = {
            name: t('html.label.newPlayers'),
            type: 'spline',
            tooltip: tooltip.zeroDecimals,
            data: data.newPlayers,
            color: nightModeEnabled ? withReducedSaturation(data.colors.newPlayers) : data.colors.newPlayers
        };
        setSeries([uniquePlayers, newPlayers]);
    }, [data, t, nightModeEnabled])

    return (
        <LineGraph id={id} series={series} alreadyOffsetTimezone/>
    )
}

export default TimeByTimeGraph