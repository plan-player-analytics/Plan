import React from 'react';
import {Col} from "react-bootstrap";
import JoinAddressGroupCard from "../../components/cards/server/graphs/JoinAddressGroupCard";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const NetworkJoinAddresses = () => {
    return (
        <LoadIn>
            <section className={"network-join-addresses"}>
                <ExtendableRow id={'row-network-join-addresses-0'}>
                    <Col lg={8}>
                        <JoinAddressGraphCard identifier={undefined}/>
                    </Col>
                    <Col lg={4}>
                        <JoinAddressGroupCard identifier={undefined}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default NetworkJoinAddresses