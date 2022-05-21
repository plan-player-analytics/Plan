import React from "react";
import {Col, Row} from "react-bootstrap-v5";
import OnlineActivityGraphsCard from "../components/cards/server/OnlineActivityGraphsCard";

const ServerOnlineActivity = () => {
    return (
        <section className="server_online_activity_overview">
            <Row>
                <Col lg={12}>
                    <OnlineActivityGraphsCard/>
                </Col>
            </Row>
            <Row>
                <Col lg={4}>
                </Col>
                <Col lg={8}>
                </Col>
            </Row>
        </section>
    )
}

export default ServerOnlineActivity