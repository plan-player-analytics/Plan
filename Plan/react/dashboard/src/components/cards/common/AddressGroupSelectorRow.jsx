import {Col, Row} from "react-bootstrap";
import AddressGroupCard from "./AddressGroupCard.jsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faPlus} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {useTranslation} from "react-i18next";
import {useJoinAddressListContext} from "../../../hooks/context/joinAddressListContextHook.jsx";
import ActionButton from "../../input/ActionButton.jsx";

const AddressGroupSelectorRow = () => {
    const {t} = useTranslation();
    const {list, add, remove, replace, allAddresses} = useJoinAddressListContext();

    return (
        <Row id={"address-selector"}>
            {list.map((group, i) =>
                <Col lg={2} key={group.uuid}>
                    <AddressGroupCard n={i + 1}
                                      group={group}
                                      editGroup={replacement => replace(replacement, i)}
                                      allAddresses={allAddresses}
                                      remove={() => remove(i)}/>
                </Col>)}
            <Col lg={2}>
                <ActionButton className={"mb-4"} onClick={add}>
                    <FontAwesomeIcon icon={faPlus}/> {t('html.label.addJoinAddressGroup')}
                </ActionButton>
            </Col>
        </Row>
    )
}

export default AddressGroupSelectorRow;