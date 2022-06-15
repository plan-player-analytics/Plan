import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPlayers} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {Col, Row} from "react-bootstrap-v5";
import PlayerListCard from "../../components/cards/common/PlayerListCard";

const AllPlayers = () => {
    const {data, loadingError} = useDataRequest(fetchPlayers, [null]);

    if (!data) return <></>;
    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <Row>
            <Col md={12}>
                <PlayerListCard data={data}/>
            </Col>
        </Row>
    )
};

export default AllPlayers