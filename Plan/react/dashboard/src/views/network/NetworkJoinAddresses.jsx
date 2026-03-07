import React from 'react';
import JoinAddresses from "../../components/cards/common/JoinAddresses.jsx";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const NetworkJoinAddresses = () => {
    const {hasPermission} = useAuth();
    const seeTime = hasPermission('page.network.join.addresses.graphs.time');
    return (
        <JoinAddresses id={'network-join-addresses'} identifier={null} seeTime={seeTime}/>
    )
};

export default NetworkJoinAddresses