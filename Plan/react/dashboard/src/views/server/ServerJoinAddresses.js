import React from 'react';
import {Col, Row} from "react-bootstrap-v5";
import JoinAddressGroupCard from "../../components/cards/server/graphs/JoinAddressGroupCard";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";

const ServerJoinAddresses = () => {


    return (
        <Row>
            <Col lg={8}>
                <JoinAddressGraphCard/>
            </Col>
            <Col lg={4}>
                <JoinAddressGroupCard/>
            </Col>
        </Row>
    )
};

export default ServerJoinAddresses