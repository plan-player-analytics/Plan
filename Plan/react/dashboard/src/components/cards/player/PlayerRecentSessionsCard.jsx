import React from "react";
import RecentSessionsCard from "../common/RecentSessionsCard";
import {useGenericFilter} from "../../../dataHooks/genericFilterContextHook.tsx";
import {useSessions} from "../../../dataHooks/sessionsHooks.ts";
import {ErrorViewCard} from "../../../views/ErrorView.tsx";

const PlayerRecentSessionsCard = ({player}) => {
    const {after, before, player: playerUUID} = useGenericFilter();
    const {data: sessions, error: loadingError} = useSessions({after, before, player: playerUUID});

    if (loadingError) {
        return <ErrorViewCard error={loadingError}/>;
    }
    return (
        <RecentSessionsCard sessions={sessions || player.sessions} isPlayer/>
    )
}

export default PlayerRecentSessionsCard;