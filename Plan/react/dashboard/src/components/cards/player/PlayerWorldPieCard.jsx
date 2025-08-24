import React from "react";
import WorldPieCard from "../common/WorldPieCard";

const PlayerWorldPieCard = ({player}) => {
    return (
        <WorldPieCard
            worldSeries={player.world_pie_series}
            gmSeries={player.gm_series}
        />
    )
}

export default PlayerWorldPieCard;