import React from 'react';
import {useParams} from "react-router-dom";
import {JoinAddressListContextProvider} from "../../hooks/context/joinAddressListContextHook.jsx";
import JoinAddresses from "../../components/cards/common/JoinAddresses.jsx";

const ServerJoinAddresses = () => {
    const {identifier} = useParams();
    return (
        <JoinAddressListContextProvider identifier={identifier}>
            <JoinAddresses id={'server-join-addresses'} identifier={identifier}
                           permission={'page.server.join.addresses.graphs.time'}/>
        </JoinAddressListContextProvider>
    )
};

export default ServerJoinAddresses