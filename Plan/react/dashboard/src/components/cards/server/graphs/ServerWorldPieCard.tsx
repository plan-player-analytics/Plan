import React from "react";
import WorldPieCard from "../../common/WorldPieCard";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {useWorldPie} from "../../../../dataHooks/graphHooks";
import {useGenericFilter} from "../../../../dataHooks/genericFilterContextHook";
import {CardLoader} from "../../../navigation/Loader";

const ServerWorldPieCard = () => {
    const {after, before, player: playerUUID} = useGenericFilter();
    const {data: worldPie, error: loadingError} = useWorldPie({after, before, player: playerUUID});

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!worldPie) return <CardLoader/>;

    return (
        <WorldPieCard
            worldSeries={worldPie.value.slices}
            gmSeries={worldPie.value.drilldown}
        />
    )
}

export default ServerWorldPieCard;