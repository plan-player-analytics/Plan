import React from "react";
import WorldPieCard from "../../common/WorldPieCard";
import {useParams} from "react-router";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchWorldPie} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";

const ServerWorldPieCard = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchWorldPie, [identifier]);

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <WorldPieCard
            worldSeries={data?.world_series}
            gmSeries={data?.gm_series}
        />
    )
}

export default ServerWorldPieCard;