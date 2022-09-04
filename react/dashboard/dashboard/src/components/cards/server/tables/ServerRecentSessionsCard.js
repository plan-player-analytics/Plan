import React from "react";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchSessions} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView";
import RecentSessionsCard from "../../common/RecentSessionsCard";

const ServerRecentSessionsCard = () => {

    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchSessions, [identifier])

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <RecentSessionsCard sessions={data?.sessions} isPlayer={true}/>
    )
}

export default ServerRecentSessionsCard;