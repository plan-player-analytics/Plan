import React from "react";
import {useDataRequest} from "../../../../hooks/dataFetchHook";
import {fetchSessions} from "../../../../service/serverService";
import {ErrorViewCard} from "../../../../views/ErrorView.tsx";
import RecentSessionsCard from "../../common/RecentSessionsCard";

const ServerRecentSessionsCard = ({identifier}) => {
    const {data, loadingError} = useDataRequest(fetchSessions, [identifier])

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <RecentSessionsCard sessions={data?.sessions} isPlayer={true} isNetwork={!identifier}/>
    )
}

export default ServerRecentSessionsCard;