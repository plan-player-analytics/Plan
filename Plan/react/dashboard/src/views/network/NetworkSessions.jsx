import {Col} from "react-bootstrap";
import React from "react";
import ServerRecentSessionsCard from "../../components/cards/server/tables/ServerRecentSessionsCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {NetworkSessionCalendarCard} from "../../components/cards/server/graphs/ServerSessionCalendarCard.tsx";
import SessionInsightsCard from "../../components/cards/server/insights/SessionInsightsCard.jsx";
import {GenericFilterContextProvider} from "../../dataHooks/genericFilterContextHook.tsx";
import {DateFilterControls} from "../../components/input/DateFilterControls.tsx";

const NetworkSessions = () => {
    const {hasPermission} = useAuth();
    const seeSessionList = hasPermission('page.network.sessions.list')
    const seeServerPie = hasPermission('page.network.sessions.server.pie')
    const seeInsights = hasPermission('page.network.sessions.overview')

    return (
        <LoadIn>
            <GenericFilterContextProvider>
                <section className="network-sessions">
                    <ExtendableRow id={'row-network-sessions-0'}>
                        {<Col lg={6}>
                            <NetworkSessionCalendarCard/>
                            <SessionInsightsCard identifier={undefined}/>
                        </Col>}
                        {seeSessionList && <Col lg={6}>
                            <DateFilterControls/>
                            <ServerRecentSessionsCard identifier={undefined}/>
                        </Col>}
                    </ExtendableRow>
                </section>
            </GenericFilterContextProvider>
        </LoadIn>
    )
}

export default NetworkSessions;