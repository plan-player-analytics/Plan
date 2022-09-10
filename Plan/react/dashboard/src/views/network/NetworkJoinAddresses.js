import React from 'react';
import {Col, Row} from "react-bootstrap-v5";
import JoinAddressGroupCard from "../../components/cards/server/graphs/JoinAddressGroupCard";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";

const NetworkJoinAddresses = () => {
    return (
        <Row>
            <Col lg={8}>
                <JoinAddressGraphCard identifier={undefined}/>
            </Col>
            <Col lg={4}>
                <JoinAddressGroupCard identifier={undefined}/>
            </Col>
        </Row>
    )
};

export default NetworkJoinAddresses