import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPlayersTable} from "../../service/serverService";
import ErrorView from "../ErrorView.tsx";
import {Col} from "react-bootstrap";
import PlayerListCard from "../../components/cards/common/PlayerListCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {CardLoader} from "../../components/navigation/Loader";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const AllPlayers = () => {
    const {hasPermission} = useAuth();
    const seePlayers = hasPermission('page.network.players') || hasPermission('access.players')
    const {data, loadingError} = useDataRequest(fetchPlayersTable, [null], seePlayers);

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            {seePlayers && <ExtendableRow id={'row-player-list-0'}>
                <Col md={12}>
                    {data ? <PlayerListCard data={data}/> : <CardLoader/>}
                </Col>
            </ExtendableRow>}
        </LoadIn>
    )
};

export default AllPlayers