import {Col} from "react-bootstrap";
import React from "react";
import LoadIn from "../../components/animation/LoadIn";
import {useParams} from "react-router";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import PluginHistoryCard from "../../components/cards/common/PluginHistoryCard";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPluginHistory} from "../../service/serverService";
import PluginCurrentCard from "../../components/cards/common/PluginCurrentCard";

const ServerPluginHistory = () => {
    const {authRequired, hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeHistory = authRequired && hasPermission('page.server.plugin.history');
    const {data, loadingError} = useDataRequest(fetchPluginHistory, [identifier], seeHistory);
    return (
        <LoadIn>
            {seeHistory && <section id="server-plugin-history">
                <ExtendableRow id={'row-server-plugin-history-0'}>
                    <Col md={6}>
                        <PluginCurrentCard data={data} loadingError={loadingError}/>
                    </Col>
                    <Col md={6}>
                        <PluginHistoryCard data={data} loadingError={loadingError}/>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
}

export default ServerPluginHistory;