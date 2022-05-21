import React from "react";
import RecentSessionsCard from "../common/RecentSessionsCard";

const PlayerRecentSessionsCard = ({player}) => {
    return (
        <RecentSessionsCard sessions={player.sessions}/>
    )
}

export default PlayerRecentSessionsCard;