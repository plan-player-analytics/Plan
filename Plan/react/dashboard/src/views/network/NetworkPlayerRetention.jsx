import React from 'react';
import {useAuth} from "../../hooks/authenticationHook";
import PlayerRetention from "../../components/cards/common/PlayerRetention.jsx";

const NetworkPlayerRetention = () => {
    const {hasPermission} = useAuth();
    const seeRetention = hasPermission('page.network.retention');
    return (
        <PlayerRetention id={"network-retention"} identifier={null} seeRetention={seeRetention}/>
    )
};

export default NetworkPlayerRetention