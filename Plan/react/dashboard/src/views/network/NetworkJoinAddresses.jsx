import React, {useCallback, useEffect, useState} from 'react';
import {Card, Col, Form, Row} from "react-bootstrap";
import JoinAddressGraphCard from "../../components/cards/server/graphs/JoinAddressGraphCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCheck, faList, faPencil, faPlus} from "@fortawesome/free-solid-svg-icons";
import MultiSelect from "../../components/input/MultiSelect.jsx";
import {useDataRequest} from "../../hooks/dataFetchHook.js";
import {fetchPlayerJoinAddresses} from "../../service/serverService.js";
import {useTranslation} from "react-i18next";
import {faTrashAlt} from "@fortawesome/free-regular-svg-icons";
import CardHeader from "../../components/cards/CardHeader.jsx";
import {randomUuid} from "../../util/uuid.js";

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
        <Col lg={2}>
            <Card>
                <CardHeader icon={faList} color={"amber"} label={
                    editingName ? <Form.Control value={name} onChange={e => setName(e.target.value)}/> : group.name
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
        </Col>
    )
}

const NetworkJoinAddresses = () => {
    const identifier = undefined;
    const {hasPermission} = useAuth();

    const seeTime = false && hasPermission('page.network.join.addresses.graphs.time');
    const seeLatest = hasPermission('page.network.join.addresses.graphs.pie');

    // TODO Move to context
    const [list, setList] = useState([]);
    const add = useCallback(() => {
        setList([...list, {name: "Address group " + (list.length + 1), addresses: [], uuid: randomUuid()}])
    }, [list, setList]);
    const remove = useCallback(index => {
        setList(list.filter((f, i) => i !== index));
    }, [setList, list]);
    const replace = useCallback((replacement, index) => {
        const newList = [...list];
        newList[index] = replacement;
        setList(newList)
    }, [setList, list]);

    const {
        data: joinAddressData,
        loadingError: joinAddressLoadingError
    } = useDataRequest(fetchPlayerJoinAddresses, [identifier]);

    let allAddresses = joinAddressData ? Object.values(joinAddressData.join_address_by_player) : [];

    function onlyUnique(value, index, array) {
        return array.indexOf(value) === index;
    }

    allAddresses = allAddresses.filter(onlyUnique);
    return (
        <LoadIn>
            <section className={"network-join-addresses"}>
                <ExtendableRow id={'row-network-join-addresses-0'}>
                    {seeTime && <JoinAddressGraphCard identifier={undefined} addresses={[]}/>}
                </ExtendableRow>
                <Row>
                    {list.map((group, i) =>
                        <AddressListCard key={group.uuid} n={i + 1}
                                         group={group}
                                         editGroup={replacement => replace(replacement, i)}
                                         allAddresses={allAddresses}
                                         remove={() => remove(i)}/>)}
                    <Col lg={2}>
                        <button className={"btn bg-theme mb-4"} onClick={add}>
                            <FontAwesomeIcon icon={faPlus}/> Add address group
                        </button>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default NetworkJoinAddresses