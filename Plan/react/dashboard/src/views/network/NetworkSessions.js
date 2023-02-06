import {Col, Row} from "react-bootstrap";
import React from "react";
import ServerRecentSessionsCard from "../../components/cards/server/tables/ServerRecentSessionsCard";
import SessionInsightsCard from "../../components/cards/server/insights/SessionInsightsCard";
import LoadIn from "../../components/animation/LoadIn";
import ServerPieCard from "../../components/cards/common/ServerPieCard";

const NetworkSessions = () => {
    return (
        <LoadIn>
            <section className="server_sessions">
                <Row>
                    <Col lg={8}>
                        <ServerRecentSessionsCard identifier={undefined}/>
                    </Col>
                    <Col lg={4}>
                        <ServerPieCard/>
                        <SessionInsightsCard identifier={undefined}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

export default NetworkSessions;