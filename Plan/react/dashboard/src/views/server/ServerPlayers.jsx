import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {useParams} from "react-router";
import {fetchPlayersTable} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {Col} from "react-bootstrap";
import PlayerListCard from "../../components/cards/common/PlayerListCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const ServerPlayers = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seePlayers = hasPermission('page.server.players')
    const {data, loadingError} = useDataRequest(fetchPlayersTable, [identifier], seePlayers);

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <section className="server-players">
                {seePlayers && <ExtendableRow id={'row-server-players-0'}>
                    <Col md={12}>
                        <PlayerListCard data={data}/>
                    </Col>
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
};

export default ServerPlayers