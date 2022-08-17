import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import {useParams} from "react-router-dom";
import {fetchPlayers} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {Col, Row} from "react-bootstrap-v5";
import PlayerListCard from "../../components/cards/common/PlayerListCard";

const ServerPlayers = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPlayers, [identifier]);

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

export default ServerPlayers