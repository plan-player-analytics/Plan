import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPlayers} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {Col} from "react-bootstrap";
import PlayerListCard from "../../components/cards/common/PlayerListCard";
import LoadIn from "../../components/animation/LoadIn";
import {CardLoader} from "../../components/navigation/Loader";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const AllPlayers = () => {
    const {data, loadingError} = useDataRequest(fetchPlayers, [null]);

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <ExtendableRow id={'row-player-list-0'}>
                <Col md={12}>
                    {data ? <PlayerListCard data={data}/> : <CardLoader/>}
                </Col>
            </ExtendableRow>
        </LoadIn>
    )
};

export default AllPlayers