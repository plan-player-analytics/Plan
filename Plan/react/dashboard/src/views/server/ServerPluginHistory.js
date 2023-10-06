import {Col} from "react-bootstrap";
import React from "react";
import LoadIn from "../../components/animation/LoadIn";
import {useParams} from "react-router-dom";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import PluginHistoryCard from "../../components/cards/common/PluginHistoryCard";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPluginHistory} from "../../service/serverService";
import PluginCurrentCard from "../../components/cards/common/PluginCurrentCard";

const ServerPluginHistory = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPluginHistory, [identifier]);
    return (
        <LoadIn>
            <section className="server-plugin-versions">
                <ExtendableRow id={'row-server-plugin-versions-0'}>
                    <Col md={6}>
                        <PluginCurrentCard data={data} loadingError={loadingError}/>
                    </Col>
                    <Col md={6}>
                        <PluginHistoryCard data={data} loadingError={loadingError}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
}

export default ServerPluginHistory;