import React from "react";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../../hooks/dataFetchHook";
import {fetchSessions} from "../../../service/serverService";
import {ErrorViewBody} from "../../../views/ErrorView";
import RecentSessionsCard from "../common/RecentSessionsCard";

const ServerRecentSessionsCard = () => {

    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchSessions, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return (
        <RecentSessionsCard sessions={data.sessions}/>
    )
}

export default ServerRecentSessionsCard;