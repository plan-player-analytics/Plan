import LoadIn from "../../animation/LoadIn.jsx";
import ExtendableRow from "../../layout/extension/ExtendableRow.jsx";
import JoinAddressGraphCard from "../server/graphs/JoinAddressGraphCard.jsx";
import {Col} from "react-bootstrap";
import React from "react";
import AddressGroupSelectorRow from "./AddressGroupSelectorRow.jsx";
import {JoinAddressListContextProvider} from "../../../hooks/context/joinAddressListContextHook.jsx";

const JoinAddresses = ({id, seeTime, identifier}) => {
    return (
        <LoadIn>
            {seeTime && <section id={id} className={id}>
                <JoinAddressListContextProvider identifier={null} isAllowed={seeTime}>
                    <ExtendableRow id={`row-${id}-0`}>
                        <Col lg={12}>
                            <JoinAddressGraphCard identifier={identifier}/>
                        </Col>
                    </ExtendableRow>
                    <AddressGroupSelectorRow/>
                </JoinAddressListContextProvider>
            </section>}
        </LoadIn>
    )
}

export default JoinAddresses;