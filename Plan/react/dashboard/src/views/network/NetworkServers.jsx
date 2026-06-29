import React, {useState} from 'react';
import {Col} from "react-bootstrap";
import ServersTableCard from "../../components/cards/network/ServersTableCard";
import QuickViewGraphCard from "../../components/cards/network/QuickViewGraphCard";
import QuickViewDataCard from "../../components/cards/network/QuickViewDataCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {useMetadata} from "../../hooks/metadataHook.tsx";

const NetworkServers = () => {
    const {hasPermission} = useAuth();
    const [selectedServer, setSelectedServer] = useState(0);

    const seeServers = hasPermission('page.network.server.list');
    const {networkMetadata} = useMetadata();
    const servers = networkMetadata?.servers?.filter(server => !server.proxy) || [];

    return (
        <>{seeServers && <ExtendableRow id={'row-network-servers-0'}>
            <Col md={6}>
                <ServersTableCard onSelect={(index) => setSelectedServer(index)} servers={servers}/>
            </Col>
            <Col md={6}>
                {servers.length && <QuickViewGraphCard server={servers[selectedServer]}/>}
                {servers.length && <QuickViewDataCard server={servers[selectedServer]}/>}
            </Col>
        </ExtendableRow>}</>
    )
};

export default NetworkServers