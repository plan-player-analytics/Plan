import React from "react";
import {ErrorViewCard} from "../../../../views/ErrorView.tsx";
import RecentSessionsCard from "../../common/RecentSessionsCard";
import {useSessions} from "../../../../dataHooks/sessionsHooks.ts";
import {useGenericFilter} from "../../../../dataHooks/genericFilterContextHook.tsx";

const ServerRecentSessionsCard = ({identifier}) => {
    const {after, before} = useGenericFilter();
    const {data: sessions, error: loadingError} = useSessions({after, before, server: identifier});

    if (loadingError) return <ErrorViewCard error={loadingError}/>

    return (
        <RecentSessionsCard sessions={sessions} isNetwork={!identifier}/>
    )
}

export default ServerRecentSessionsCard;