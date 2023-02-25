import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {useParams} from "react-router-dom";
import {fetchPlayers} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {Col} from "react-bootstrap";
import PlayerListCard from "../../components/cards/common/PlayerListCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const ServerPlayers = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPlayers, [identifier]);

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <section className="server-players">
                <ExtendableRow id={'row-server-players-0'}>
                    <Col md={12}>
                        <PlayerListCard data={data}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default ServerPlayers