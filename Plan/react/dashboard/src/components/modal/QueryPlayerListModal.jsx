import React from 'react';
import {useTranslation} from "react-i18next";
import {Modal} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faArrowRight, faSearch} from "@fortawesome/free-solid-svg-icons";
import {getViewTitle} from "../../views/query/QueryResultView";
import {ChartLoader} from "../navigation/Loader";
import {Link} from "react-router-dom";
import {useAuth} from "../../hooks/authenticationHook";
import PlayerTable from "../table/PlayerTable.jsx";

const QueryPlayerListModal = ({open, toggle, queryData, title}) => {
    const {t} = useTranslation();
    const {hasPermission} = useAuth();
    return (
        <Modal id="queryModal" aria-labelledby="queryModalLabel" show={open} onHide={toggle} size="xl">
            <Modal.Header>
                <Modal.Title id="queryModalLabel">
                    <Fa icon={faSearch}/> {queryData ? title || getViewTitle(queryData, t, true) : t('html.query.title.text')}
                </Modal.Title>
                <button aria-label="Close" className="btn-close" type="button" onClick={toggle}/>
            </Modal.Header>
            {!queryData && <ChartLoader/>}
            {queryData &&
                <PlayerTable data={queryData?.data?.players || {players: [], extensionDescriptors: []}}
                             orderBy={2}/>}
            <Modal.Footer>
                {hasPermission('access.query') && Boolean(queryData?.data?.players.players.length) &&
                    <Link className="btn bg-theme"
                          to={"/query/result?timestamp=" + queryData?.timestamp}>
                        {t('html.query.label.showFullQuery')} <Fa icon={faArrowRight}/>
                    </Link>}
                <button className="btn bg-theme" onClick={toggle}>OK</button>
            </Modal.Footer>
        </Modal>
    )
};

export default QueryPlayerListModal