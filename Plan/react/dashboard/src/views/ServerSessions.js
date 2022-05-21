import {Col, Row} from "react-bootstrap-v5";
import React from "react";
import ServerWorldPieCard from "../components/cards/server/ServerWorldPieCard";
import ServerRecentSessionsCard from "../components/cards/server/ServerRecentSessionsCard";
import SessionInsightsCard from "../components/cards/server/SessionInsightsCard";

const ServerSessions = () => {
    return (
        <section className="server_sessions">
            <Row>
                <Col lg={8}>
                    <ServerRecentSessionsCard/>
                </Col>
                <Col lg={4}>
                    <ServerWorldPieCard/>
                    <SessionInsightsCard/>
                </Col>
            </Row>
        </section>
    )
}

export default ServerSessions;