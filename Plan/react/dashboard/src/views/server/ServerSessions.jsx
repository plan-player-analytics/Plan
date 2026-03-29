import {Col} from "react-bootstrap";
import React from "react";
import ServerRecentSessionsCard from "../../components/cards/server/tables/ServerRecentSessionsCard";
import SessionInsightsCard from "../../components/cards/server/insights/SessionInsightsCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {useParams} from "react-router";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {ServerSessionCalendarCard} from "../../components/cards/server/graphs/ServerSessionCalendarCard.tsx";
import {GenericFilterContextProvider} from "../../dataHooks/genericFilterContextHook.tsx";
import {DateFilterControls} from "../../components/input/DateFilterControls.tsx";
import ServerWorldPieCard from "../../components/cards/server/graphs/ServerWorldPieCard.tsx";

const ServerSessions = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeSessionList = hasPermission('page.server.sessions.list');
    const seeSessionInsights = hasPermission('page.server.sessions.overview');
    const seeWorldPie = hasPermission('page.server.sessions.world.pie');
    const seeControls = seeSessionList || seeSessionInsights || seeWorldPie;
    return (
        <LoadIn>
            <GenericFilterContextProvider initialValue={{server: identifier}}>
                <section className="server-sessions">
                    <ExtendableRow id={'row-server-sessions-0'}>
                        {<Col lg={6}>
                            <ServerSessionCalendarCard identifier={identifier}/>
                            {seeSessionInsights && <SessionInsightsCard identifier={identifier}/>}
                        </Col>}
                        <Col lg={6}>
                            {seeControls && <DateFilterControls/>}
                            {seeSessionList && <ServerRecentSessionsCard identifier={identifier}/>}
                            {seeWorldPie && <ServerWorldPieCard/>}
                        </Col>
                    </ExtendableRow>
                </section>
            </GenericFilterContextProvider>
        </LoadIn>
    )
}

export default ServerSessions;