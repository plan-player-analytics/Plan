import {Col, Row} from "react-bootstrap-v5";
import React from "react";
import PlayerbaseDevelopmentCard from "../../components/cards/server/graphs/PlayerbaseDevelopmentCard";
import CurrentPlayerbaseCard from "../../components/cards/server/graphs/CurrentPlayerbaseCard";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPlayerbaseOverview} from "../../service/serverService";
import {ErrorViewCard} from "../ErrorView";
import PlayerbaseTrendsCard from "../../components/cards/server/tables/PlayerbaseTrendsCard";
import PlayerbaseInsightsCard from "../../components/cards/server/insights/PlayerbaseInsightsCard";
import LoadIn from "../../components/animation/LoadIn";

const PlayerbaseOverview = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPlayerbaseOverview, [identifier]);

    return (
        <LoadIn>
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
                    {loadingError && <ErrorViewCard error={loadingError}/>}
                    {!loadingError && <>
                        <Col lg={8}>
                            <PlayerbaseTrendsCard data={data?.trends}/>
                        </Col>
                        <Col lg={4}>
                            <PlayerbaseInsightsCard data={data?.insights}/>
                        </Col>
                    </>}
                </Row>
            </section>
        </LoadIn>
    )
}

export default PlayerbaseOverview;