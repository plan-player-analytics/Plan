import {Col, Row} from "react-bootstrap";
import React from "react";
import ServerWorldPieCard from "../../components/cards/server/graphs/ServerWorldPieCard";
import ServerRecentSessionsCard from "../../components/cards/server/tables/ServerRecentSessionsCard";
import SessionInsightsCard from "../../components/cards/server/insights/SessionInsightsCard";
import LoadIn from "../../components/animation/LoadIn";
import {useParams} from "react-router-dom";

const ServerSessions = () => {
    const {identifier} = useParams();
    return (
        <LoadIn>
            <section className="server_sessions">
                <Row>
                    <Col lg={8}>
                        <ServerRecentSessionsCard identifier={identifier}/>
                    </Col>
                    <Col lg={4}>
                        <ServerWorldPieCard/>
                        <SessionInsightsCard identifier={identifier}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

export default ServerSessions;