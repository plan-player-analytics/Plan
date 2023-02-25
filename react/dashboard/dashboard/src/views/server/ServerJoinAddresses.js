import React from 'react';
import {Col} from "react-bootstrap";
import JoinAddressGroupCard from "../../components/cards/server/graphs/JoinAddressGroupCard";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";
import {useParams} from "react-router-dom";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const ServerJoinAddresses = () => {
    const {identifier} = useParams();
    return (
        <LoadIn>
            <section className={"server-join-addresses"}>
                <ExtendableRow id={'row-server-join-addresses-0'}>
                    <Col lg={8}>
                        <JoinAddressGraphCard identifier={identifier}/>
                    </Col>
                    <Col lg={4}>
                        <JoinAddressGroupCard identifier={identifier}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default ServerJoinAddresses