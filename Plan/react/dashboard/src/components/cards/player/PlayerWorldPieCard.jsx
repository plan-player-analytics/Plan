import React from "react";
import WorldPieCard from "../common/WorldPieCard";
import {useGenericFilter} from "../../../dataHooks/genericFilterContextHook.tsx";
import {useSessions} from "../../../dataHooks/sessionsHooks.ts";
import {ErrorViewCard} from "../../../views/ErrorView.tsx";

const PlayerWorldPieCard = ({player}) => {
    const {after, before, player: playerUUID} = useGenericFilter();
    const {data: sessions, error: loadingError} = useSessions({after, before, player: playerUUID});

    if (loadingError) return <ErrorViewCard error={loadingError}/>;
    return (
        <WorldPieCard
            worldSeries={player.world_pie_series}
            gmSeries={player.gm_series}
        />
    )
}

export default PlayerWorldPieCard;