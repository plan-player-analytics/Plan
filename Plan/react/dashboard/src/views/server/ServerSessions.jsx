import {Col} from "react-bootstrap";
import React from "react";
import ServerWorldPieCard from "../../components/cards/server/graphs/ServerWorldPieCard";
import ServerRecentSessionsCard from "../../components/cards/server/tables/ServerRecentSessionsCard";
import SessionInsightsCard from "../../components/cards/server/insights/SessionInsightsCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {useParams} from "react-router";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const ServerSessions = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeSessionList = hasPermission('page.server.sessions.list');
    const seeSessionInsights = hasPermission('page.server.sessions.overview');
    const seeWorldPie = hasPermission('page.server.sessions.world.pie');
    return (
        <LoadIn>
            <section className="server-sessions">
                <ExtendableRow id={'row-server-sessions-0'}>
                    {seeSessionList && <Col lg={8}>
                        <ServerRecentSessionsCard identifier={identifier}/>
                    </Col>}
                    <Col lg={4}>
                        {seeWorldPie && <ServerWorldPieCard/>}
                        {seeSessionInsights && <SessionInsightsCard identifier={identifier}/>}
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
}

export default ServerSessions;