import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {useParams} from "react-router";
import {fetchAllowlistBounces} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {Col} from "react-bootstrap";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import AllowlistBounceTableCard from "../../components/cards/server/tables/AllowlistBounceTableCard.jsx";

const ServerAllowList = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeBounce = hasPermission('page.server.allowlist.bounce');
    const {data, loadingError} = useDataRequest(fetchAllowlistBounces, [identifier], seeBounce);

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <section className="server-allowlist">
                {seeBounce && <ExtendableRow id={'row-server-allowlist-0'}>
                    <Col md={12}>
                        <AllowlistBounceTableCard bounces={data?.allowlist_bounces || []}
                                                  lastSeen={data?.last_seen_by_uuid || {}}/>
                    </Col>
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
};

export default ServerAllowList