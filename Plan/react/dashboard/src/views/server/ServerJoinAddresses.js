import React from 'react';
import {Col, Row} from "react-bootstrap";
import JoinAddressGroupCard from "../../components/cards/server/graphs/JoinAddressGroupCard";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";
import {useParams} from "react-router-dom";
import LoadIn from "../../components/animation/LoadIn";

const ServerJoinAddresses = () => {
    const {identifier} = useParams();
    return (
        <LoadIn>
            <section className={"server_join_addresses"}>
                <Row>
                    <Col lg={8}>
                        <JoinAddressGraphCard identifier={identifier}/>
                    </Col>
                    <Col lg={4}>
                        <JoinAddressGroupCard identifier={identifier}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default ServerJoinAddresses