import React from "react";
import WorldPieCard from "../common/WorldPieCard";
import {useGenericFilter} from "../../../dataHooks/genericFilterContextHook.tsx";
import {ErrorViewCard} from "../../../views/ErrorView.tsx";
import {useWorldPie} from "../../../dataHooks/graphHooks.ts";
import {TitleWithDates} from "../../text/TitleWithDates.tsx";

const PlayerWorldPieCard = ({player}) => {
    const filter = useGenericFilter();
    const {data: worldPie, error: loadingError} = useWorldPie(filter);

    if (loadingError) return <ErrorViewCard error={loadingError}/>;

    const title = <TitleWithDates label={'html.label.worldPlaytime'} fallback={'html.label.worldPlaytime'}
                                  after={filter.after} before={filter.before}/>;
    return (
        <WorldPieCard
            worldSeries={worldPie ? worldPie.value.slices : player.world_pie_series}
            gmSeries={worldPie ? worldPie.value.drilldown : player.gm_series}
            title={title}
        />
    )
}

export default PlayerWorldPieCard;