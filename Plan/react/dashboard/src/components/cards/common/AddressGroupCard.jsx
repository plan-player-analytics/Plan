import {useTranslation} from "react-i18next";
import React, {useCallback, useEffect, useState} from "react";
import {Card, Form} from "react-bootstrap";
import CardHeader from "../CardHeader.jsx";
import {faCheck, faList, faPencil} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import MultiSelect from "../../input/MultiSelect.jsx";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";
import ActionButton from "../../input/button/ActionButton.jsx";
import OutlineButton from "../../input/button/OutlineButton.jsx";

const AddressGroupCard = ({n, group, editGroup, allAddresses, remove}) => {
    const {t} = useTranslation();
    const [selectedIndexes, setSelectedIndexes] = useState([]);
    const [editingName, setEditingName] = useState(false);
    const [name, setName] = useState(group.name);

    useEffect(() => {
        if (!selectedIndexes.length && allAddresses?.length && group?.addresses?.length) {
            setSelectedIndexes(group.addresses
                .map(address => allAddresses.indexOf(address))
                .filter(index => index !== -1)) // Make sure addresses are not selected that no longer exist
        }
    }, [selectedIndexes, group, allAddresses])

    const applySelected = useCallback(() => {
        editGroup({...group, addresses: allAddresses.filter((a, i) => selectedIndexes.includes(i))})
    }, [editGroup, group, allAddresses, selectedIndexes]);
    const editName = useCallback(newName => {
        editGroup({...group, name: newName});
    }, [editGroup, group]);
    useEffect(() => {
        if (!editingName && name !== group.name) editName(name);
    }, [editName, editingName, name])

    const selectedAddresses = allAddresses.filter((a, i) => selectedIndexes.includes(i));
    const isUpToDate = !selectedIndexes.length || selectedAddresses.length === group.addresses.length && selectedAddresses.every((a, i) => a === group.addresses[i]);
    return (
        <Card>
            <CardHeader icon={faList} color={"join-addresses"} label={
                editingName ?
                    <Form.Control
                        style={{position: "absolute", top: "0.5rem", left: "2.5rem", width: "calc(100% - 3rem)"}}
                        value={name}
                        onChange={e => setName(e.target.value)}/> : group.name
            }>
                <button
                    style={editingName ? {position: "absolute", right: "1rem", top: "1.2rem"} : {marginLeft: "0.5rem"}}
                    onClick={() => setEditingName(!editingName)}>
                    <FontAwesomeIcon icon={editingName ? faCheck : faPencil}/>
                </button>
            </CardHeader>
            <Card.Body>
                <MultiSelect options={allAddresses} selectedIndexes={selectedIndexes}
                             setSelectedIndexes={setSelectedIndexes}/>
                <ActionButton className={'mt-2'} onClick={applySelected} disabled={isUpToDate}>
                    {t('html.label.apply')}
                </ActionButton>
                <OutlineButton className={'mt-2 float-end'} onClick={remove}>
                    <FontAwesomeIcon icon={faTrashAlt}/>
                </OutlineButton>
            </Card.Body>
        </Card>
    )
}
export default AddressGroupCard;