import React, {useState} from 'react';
import {Col} from "react-bootstrap";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchServersOverview} from "../../service/networkService";
import ErrorView from "../ErrorView.tsx";
import ServersTableCard from "../../components/cards/network/ServersTableCard";
import QuickViewGraphCard from "../../components/cards/network/QuickViewGraphCard";
import QuickViewDataCard from "../../components/cards/network/QuickViewDataCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const NetworkServers = () => {
    const {hasPermission} = useAuth();
    const [selectedServer, setSelectedServer] = useState(0);

    const seeServers = hasPermission('page.network.server.list');
    const {data, loadingError} = useDataRequest(fetchServersOverview, [], seeServers);

    if (loadingError) {
        return <ErrorView error={loadingError}/>
    }

    return (
        <>{seeServers && <ExtendableRow id={'row-network-servers-0'}>
            <Col md={6}>
                <ServersTableCard loaded={Boolean(data)} servers={data?.servers || []}
                                  onSelect={(index) => setSelectedServer(index)}/>
            </Col>
            <Col md={6}>
                {data?.servers.length && <QuickViewGraphCard server={data.servers[selectedServer]}/>}
                {data?.servers.length && <QuickViewDataCard server={data.servers[selectedServer]}/>}
            </Col>
        </ExtendableRow>}</>
    )
};

export default NetworkServers