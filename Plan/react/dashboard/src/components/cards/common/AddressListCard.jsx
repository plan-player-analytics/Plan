import {useTranslation} from "react-i18next";
import React, {useCallback, useEffect, useState} from "react";
import {Card, Form} from "react-bootstrap";
import CardHeader from "../CardHeader.jsx";
import {faCheck, faList, faPencil} from "@fortawesome/free-solid-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import MultiSelect from "../../input/MultiSelect.jsx";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";

const AddressListCard = ({n, group, editGroup, allAddresses, remove}) => {
    const {t} = useTranslation();
    const [selectedIndexes, setSelectedIndexes] = useState([]);
    const [editingName, setEditingName] = useState(false);
    const [name, setName] = useState(group.name);

    const isUpToDate = group.addresses === allAddresses.filter((a, i) => selectedIndexes.includes(i));
    const applySelected = useCallback(() => {
        editGroup({...group, addresses: allAddresses.filter((a, i) => selectedIndexes.includes(i))})
    }, [editGroup, group, allAddresses, selectedIndexes]);
    const editName = useCallback(newName => {
        editGroup({...group, name: newName});
    }, [editGroup, group]);
    useEffect(() => {
        if (!editingName && name !== group.name) editName(name);
    }, [editName, editingName, name])

    return (
        <Card>
            <CardHeader icon={faList} color={"amber"} label={
                editingName ?
                    <Form.Control style={{maxWidth: "75%", marginTop: "-1rem", marginBottom: "-1rem"}} value={name}
                                  onChange={e => setName(e.target.value)}/> : group.name
            }>
                <button style={{marginLeft: "0.5rem"}} onClick={() => setEditingName(!editingName)}>
                    <FontAwesomeIcon icon={editingName ? faCheck : faPencil}/>
                </button>
            </CardHeader>
            <Card.Body>
                <MultiSelect options={allAddresses} selectedIndexes={selectedIndexes}
                             setSelectedIndexes={setSelectedIndexes}/>
                <button className={'mt-2 btn ' + (isUpToDate ? 'bg-transparent' : 'bg-theme')}
                        onClick={applySelected} disabled={isUpToDate}>
                    {t('html.label.apply')}
                </button>
                <button className={'mt-2 btn btn-outline-secondary float-end'}
                        onClick={remove}>
                    <FontAwesomeIcon icon={faTrashAlt}/>
                </button>
            </Card.Body>
        </Card>
    )
}
export default AddressListCard;