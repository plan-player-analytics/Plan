import React from 'react';
import {Col, Row} from "react-bootstrap-v5";
import JoinAddressGroupCard from "../../components/cards/server/graphs/JoinAddressGroupCard";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";
import {useParams} from "react-router-dom";

const ServerJoinAddresses = () => {
    const {identifier} = useParams();
    return (
        <Row>
            <Col lg={8}>
                <JoinAddressGraphCard identifier={identifier}/>
            </Col>
            <Col lg={4}>
                <JoinAddressGroupCard identifier={identifier}/>
            </Col>
        </Row>
    )
};

export default ServerJoinAddresses