import React from 'react';
import JoinAddresses from "../../components/cards/common/JoinAddresses.jsx";
import {JoinAddressListContextProvider} from "../../hooks/context/joinAddressListContextHook.jsx";

const NetworkJoinAddresses = () => {
    return (
        <JoinAddressListContextProvider identifier={null}>
            <JoinAddresses id={'network-join-addresses'} identifier={null}
                           permission={'page.network.join.addresses.graphs.time'}/>
        </JoinAddressListContextProvider>
    )
};

export default NetworkJoinAddresses