import React from 'react';
import {useParams} from "react-router";
import {useAuth} from "../../hooks/authenticationHook";
import PlayerRetention from "../../components/cards/common/PlayerRetention.jsx";

const ServerPlayerRetention = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeRetention = hasPermission('page.server.retention');
    return (
        <PlayerRetention id={"server-retention"} identifier={identifier} seeRetention={seeRetention}/>
    )
};

export default ServerPlayerRetention