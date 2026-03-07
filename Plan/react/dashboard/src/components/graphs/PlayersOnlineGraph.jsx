import React, {useMemo, useState} from "react";
import {useTranslation} from "react-i18next";
import {tooltip} from "../../util/graphs";
import LineGraph from "./LineGraph";
import {ChartLoader} from "../navigation/Loader";
import {PlayersOnlineTooltip, useTooltipOptions} from "./tooltip/PlayersOnlineTooltip.jsx";


const PlayersOnlineGraph = ({data, identifier, selectedRange, extremes, onSetExtremes, color, showPlayersOnline}) => {
    const {t} = useTranslation();

    const series = useMemo(() => {
        if (!data) return [];
        const playersOnlineSeries = {
            name: t('html.label.playersOnline'),
            type: 'areaspline',
            tooltip: tooltip.zeroDecimals,
            data: data.playersOnline,
            color: color || "var(--color-graphs-players-online)",
            yAxis: 0
        }
        return [playersOnlineSeries];
    }, [data, t]);

    const [hoveredDate, setHoveredDate] = useState(undefined);
    const onMouseLeave = () => setHoveredDate(undefined);
    const extraOptions = useTooltipOptions(showPlayersOnline, setHoveredDate);

    if (!data) return <ChartLoader/>;

    return (
        <>
            {showPlayersOnline && (
                <PlayersOnlineTooltip id="players-online-graph" hoveredDate={hoveredDate} identifier={identifier}/>
            )}
            <LineGraph id="players-online-graph"
                       series={series}
                       selectedRange={selectedRange}
                       extremes={extremes}
                       onSetExtremes={onSetExtremes}
                       extraOptions={extraOptions}
                       onMouseLeave={onMouseLeave}/>
        </>
    )
}

export default PlayersOnlineGraph