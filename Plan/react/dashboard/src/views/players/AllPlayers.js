import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPlayers} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {Col, Row} from "react-bootstrap";
import PlayerListCard from "../../components/cards/common/PlayerListCard";
import LoadIn from "../../components/animation/LoadIn";
import {CardLoader} from "../../components/navigation/Loader";

const AllPlayers = () => {
    const {data, loadingError} = useDataRequest(fetchPlayers, [null]);

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <Row>
                <Col md={12}>
                    {data ? <PlayerListCard data={data}/> : <CardLoader/>}
                </Col>
            </Row>
        </LoadIn>
    )
};

export default AllPlayers