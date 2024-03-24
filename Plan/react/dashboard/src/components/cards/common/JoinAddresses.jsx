import LoadIn from "../../animation/LoadIn.jsx";
import ExtendableRow from "../../layout/extension/ExtendableRow.jsx";
import JoinAddressGraphCard from "../server/graphs/JoinAddressGraphCard.jsx";
import {Col, Row} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {useAuth} from "../../../hooks/authenticationHook.jsx";
import {useTranslation} from "react-i18next";
import {useJoinAddressListContext} from "../../../hooks/context/joinAddressListContextHook.jsx";
import AddressListCard from "./AddressListCard.jsx";

const JoinAddresses = ({id, permission, identifier}) => {
    const {t} = useTranslation();
    const {hasPermission} = useAuth();
    const {list, add, remove, replace, allAddresses} = useJoinAddressListContext();

    const seeTime = hasPermission(permission);

    return (
        <LoadIn>
            <section className={id}>
                <ExtendableRow id={`row-${id}-0`}>
                    <Col lg={12}>
                        {seeTime && <JoinAddressGraphCard identifier={identifier}/>}
                    </Col>
                </ExtendableRow>
                <Row>
                    {list.map((group, i) =>
                        <Col lg={2} key={group.uuid}>
                            <AddressListCard n={i + 1}
                                             group={group}
                                             editGroup={replacement => replace(replacement, i)}
                                             allAddresses={allAddresses}
                                             remove={() => remove(i)}/>
                        </Col>)}
                    <Col lg={2}>
                        <button className={"btn bg-theme mb-4"} onClick={add}>
                            <FontAwesomeIcon icon={faPlus}/> Add address group
                        </button>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

export default JoinAddresses;