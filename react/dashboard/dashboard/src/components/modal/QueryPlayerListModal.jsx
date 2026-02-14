import React from 'react';
import {useTranslation} from "react-i18next";
import {Modal} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faArrowRight, faSearch} from "@fortawesome/free-solid-svg-icons";
import {getViewTitle} from "../../views/query/QueryResultView";
import {ChartLoader} from "../navigation/Loader";
import {useNavigation} from "react-router";
import {useAuth} from "../../hooks/authenticationHook";
import PlayerTable from "../table/PlayerTable.jsx";
import ActionButton from "../input/button/ActionButton.jsx";
import ModalCloseButton from "../input/button/ModalCloseButton.jsx";

const QueryPlayerListModal = ({open, toggle, queryData, title}) => {
    const {t} = useTranslation();
    const navigate = useNavigation();
    const {hasPermission} = useAuth();
    return (
        <Modal id="queryModal" aria-labelledby="queryModalLabel" show={open} onHide={toggle} size="xl">
            <Modal.Header>
                <Modal.Title id="queryModalLabel">
                    <Fa icon={faSearch}/> {queryData ? title || getViewTitle(queryData, t, true) : t('html.query.title.text')}
                </Modal.Title>
                <ModalCloseButton onClick={toggle}/>
            </Modal.Header>
            {!queryData && <ChartLoader/>}
            {queryData &&
                <PlayerTable data={queryData?.data?.players || {players: [], extensionDescriptors: []}}
                             orderBy={2}/>}
            <Modal.Footer>
                {hasPermission('access.query') && Boolean(queryData?.data?.players.players.length) &&
                    <ActionButton onClick={() => navigate("/query/result?timestamp=" + queryData?.timestamp)}>
                        {t('html.query.label.showFullQuery')} <Fa icon={faArrowRight}/>
                    </ActionButton>}
                <ActionButton onClick={toggle}>OK</ActionButton>
            </Modal.Footer>
        </Modal>
    )
};

export default QueryPlayerListModal