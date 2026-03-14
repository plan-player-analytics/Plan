import React from "react";
import WorldPieCard from "../common/WorldPieCard";
import {useGenericFilter} from "../../../dataHooks/genericFilterContextHook.tsx";
import {ErrorViewCard} from "../../../views/ErrorView.tsx";
import {useWorldPie} from "../../../dataHooks/graphHooks.ts";

const PlayerWorldPieCard = ({player}) => {
    const {after, before, player: playerUUID} = useGenericFilter();
    const {data: worldPie, error: loadingError} = useWorldPie({after, before, player: playerUUID});

    if (loadingError) return <ErrorViewCard error={loadingError}/>;
    return (
        <WorldPieCard
            worldSeries={worldPie ? worldPie.value.slices : player.world_pie_series}
            gmSeries={worldPie ? worldPie.value.drilldown : player.gm_series}
        />
    )
}

export default PlayerWorldPieCard;