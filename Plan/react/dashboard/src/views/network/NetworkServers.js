import React, {useState} from 'react';
import {Col, Row} from "react-bootstrap-v5";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchServersOverview} from "../../service/networkService";
import ErrorView from "../ErrorView";
import ServersTableCard from "../../components/cards/network/ServersTableCard";
import QuickViewGraphCard from "../../components/cards/network/QuickViewGraphCard";
import QuickViewDataCard from "../../components/cards/network/QuickViewDataCard";

const NetworkServers = () => {
    const [selectedServer, setSelectedServer] = useState(0);

    const {data, loadingError} = useDataRequest(fetchServersOverview, [])

    if (loadingError) {
        return <ErrorView error={loadingError}/>
    }

    return (
        <Row>
            <Col md={6}>
                <ServersTableCard loaded={Boolean(data)} servers={data?.servers || []}
                                  onSelect={(index) => setSelectedServer(index)}/>
            </Col>
            <Col md={6}>
                {data?.servers.length && <QuickViewGraphCard server={data.servers[selectedServer]}/>}
                {data?.servers.length && <QuickViewDataCard server={data.servers[selectedServer]}/>}
            </Col>
        </Row>
    )
};

export default NetworkServers