import {Col} from "react-bootstrap";
import React from "react";
import ServerRecentSessionsCard from "../../components/cards/server/tables/ServerRecentSessionsCard";
import SessionInsightsCard from "../../components/cards/server/insights/SessionInsightsCard";
import LoadIn from "../../components/animation/LoadIn";
import ServerPieCard from "../../components/cards/common/ServerPieCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const NetworkSessions = () => {
    return (
        <LoadIn>
            <section className="network-sessions">
                <ExtendableRow id={'row-network-sessions-0'}>
                    <Col lg={8}>
                        <ServerRecentSessionsCard identifier={undefined}/>
                    </Col>
                    <Col lg={4}>
                        <ServerPieCard/>
                        <SessionInsightsCard identifier={undefined}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
}

export default NetworkSessions;