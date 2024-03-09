import React from 'react';
import {Col} from "react-bootstrap";
import JoinAddressGroupCard from "../../components/cards/server/graphs/JoinAddressGroupCard";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";
import {useParams} from "react-router-dom";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const ServerJoinAddresses = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeTime = hasPermission('page.server.join.addresses.graphs.time');
    const seeLatest = hasPermission('page.server.join.addresses.graphs.pie');
    return (
        <LoadIn>
            <section className={"server-join-addresses"}>
                <ExtendableRow id={'row-server-join-addresses-0'}>
                    {seeTime && <Col lg={8}>
                        <JoinAddressGraphCard identifier={identifier}/>
                    </Col>}
                    {seeLatest && <Col lg={4}>
                        <JoinAddressGroupCard identifier={identifier}/>
                    </Col>}
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default ServerJoinAddresses