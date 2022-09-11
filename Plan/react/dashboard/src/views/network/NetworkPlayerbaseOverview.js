import {Col, Row} from "react-bootstrap-v5";
import React from "react";
import PlayerbaseDevelopmentCard from "../../components/cards/server/graphs/PlayerbaseDevelopmentCard";
import CurrentPlayerbaseCard from "../../components/cards/server/graphs/CurrentPlayerbaseCard";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {ErrorViewCard} from "../ErrorView";
import PlayerbaseTrendsCard from "../../components/cards/server/tables/PlayerbaseTrendsCard";
import PlayerbaseInsightsCard from "../../components/cards/server/insights/PlayerbaseInsightsCard";
import LoadIn from "../../components/animation/LoadIn";
import {fetchNetworkPlayerbaseOverview} from "../../service/networkService";

const NetworkPlayerbaseOverview = () => {
    const {data, loadingError} = useDataRequest(fetchNetworkPlayerbaseOverview, []);

    return (
        <LoadIn>
            <section className="network_playerbase">
                <Row>
                    <Col lg={8}>
                        <PlayerbaseDevelopmentCard identifier={undefined}/>
                    </Col>
                    <Col lg={4}>
                        <CurrentPlayerbaseCard identifier={undefined}/>
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

export default NetworkPlayerbaseOverview;