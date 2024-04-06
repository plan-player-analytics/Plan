import LoadIn from "../../animation/LoadIn.jsx";
import ExtendableRow from "../../layout/extension/ExtendableRow.jsx";
import JoinAddressGraphCard from "../server/graphs/JoinAddressGraphCard.jsx";
import {Col} from "react-bootstrap";
import React from "react";
import {useAuth} from "../../../hooks/authenticationHook.jsx";
import AddressGroupSelectorRow from "./AddressGroupSelectorRow.jsx";

const JoinAddresses = ({id, permission, identifier}) => {
    const {hasPermission} = useAuth();
    const seeTime = hasPermission(permission);

    return (
        <LoadIn>
            {seeTime && <section id={id} className={id}>
                <ExtendableRow id={`row-${id}-0`}>
                    <Col lg={12}>
                        <JoinAddressGraphCard identifier={identifier}/>
                    </Col>
                </ExtendableRow>
                <AddressGroupSelectorRow/>
            </section>}
        </LoadIn>
    )
}

export default JoinAddresses;