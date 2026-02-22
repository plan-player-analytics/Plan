import {Col} from "react-bootstrap";
import React from "react";
import ServerRecentSessionsCard from "../../components/cards/server/tables/ServerRecentSessionsCard";
import SessionInsightsCard from "../../components/cards/server/insights/SessionInsightsCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import ServerPieCard from "../../components/cards/common/ServerPieCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const NetworkSessions = () => {
    const {hasPermission} = useAuth();
    const seeSessionList = hasPermission('page.network.sessions.list')
    const seeServerPie = hasPermission('page.network.sessions.server.pie')
    const seeInsights = hasPermission('page.network.sessions.overview')
    return (
        <LoadIn>
            <section className="network-sessions">
                <ExtendableRow id={'row-network-sessions-0'}>
                    {seeSessionList && <Col lg={8}>
                        <ServerRecentSessionsCard identifier={undefined}/>
                    </Col>}
                    <Col lg={4}>
                        {seeServerPie && <ServerPieCard/>}
                        {seeInsights && <SessionInsightsCard identifier={undefined}/>}
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
}

export default NetworkSessions;