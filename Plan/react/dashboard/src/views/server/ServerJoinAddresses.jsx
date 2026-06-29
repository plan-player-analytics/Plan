import React from 'react';
import {useParams} from "react-router";
import JoinAddresses from "../../components/cards/common/JoinAddresses.jsx";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const ServerJoinAddresses = () => {
    const {identifier} = useParams();
    const {hasPermission} = useAuth();
    const seeTime = hasPermission('page.server.join.addresses.graphs.time');
    return (
        <JoinAddresses id={'server-join-addresses'} identifier={identifier} seeTime={seeTime}/>
    )
};

export default ServerJoinAddresses