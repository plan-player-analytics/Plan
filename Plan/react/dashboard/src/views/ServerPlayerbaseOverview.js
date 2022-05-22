import {Col, Row} from "react-bootstrap-v5";
import React from "react";
import PlayerbaseDevelopmentCard from "../components/cards/server/PlayerbaseDevelopmentCard";
import CurrentPlayerbaseCard from "../components/cards/server/CurrentPlayerbaseCard";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../hooks/dataFetchHook";
import {fetchPlayerbaseOverview} from "../service/serverService";
import ErrorView from "./ErrorView";

const ServerPlayerbaseOverview = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPlayerbaseOverview, [identifier]);

    if (!data) return <></>;
    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <section className="server_playerbase">
            <Row>
                <Col lg={8}>
                    <PlayerbaseDevelopmentCard/>
                </Col>
                <Col lg={4}>
                    <CurrentPlayerbaseCard/>
                </Col>
            </Row>
            <Row>
                <Col lg={8}>

                </Col>
                <Col lg={4}>

                </Col>
            </Row>
        </section>

    )
}

export default ServerPlayerbaseOverview;