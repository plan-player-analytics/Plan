import React from "react";
import WorldPieCard from "../../common/WorldPieCard";
import {ErrorViewCard} from "../../../../views/ErrorView";
import {useWorldPie} from "../../../../dataHooks/graphHooks";
import {useGenericFilter} from "../../../../dataHooks/genericFilterContextHook";
import {CardLoader} from "../../../navigation/Loader";
import {TitleWithDates} from "../../../text/TitleWithDates";

const ServerWorldPieCard = () => {
    const filter = useGenericFilter();
    const {data: worldPie, error: loadingError} = useWorldPie(filter);
    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!worldPie) return <CardLoader/>;

    const title = <TitleWithDates label={'html.label.worldPlaytime'} fallback={'html.label.worldPlaytime'}
                                  after={filter.after} before={filter.before}/>;
    return (
        <WorldPieCard
            worldSeries={worldPie.value.slices}
            gmSeries={worldPie.value.drilldown}
            title={title}
        />
    )
}

export default ServerWorldPieCard;