import React from "react";
import {Col, Row} from "react-bootstrap";
import OnlineActivityGraphsCard from "../../components/cards/server/graphs/OnlineActivityGraphsCard";
import OnlineActivityAsNumbersCard from "../../components/cards/server/tables/OnlineActivityAsNumbersCard";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchOnlineActivityOverview} from "../../service/serverService";
import ErrorView from "../ErrorView";
import OnlineActivityInsightsCard from "../../components/cards/server/insights/OnlineActivityInsightsCard";
import LoadIn from "../../components/animation/LoadIn";

const OnlineActivity = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchOnlineActivityOverview, [identifier])

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <section className="server_online_activity_overview">
                <Row>
                    <Col lg={12}>
                        <OnlineActivityGraphsCard/>
                    </Col>
                </Row>
                <Row>
                    <Col lg={8}>
                        <OnlineActivityAsNumbersCard data={data?.numbers}/>
                    </Col>
                    <Col lg={4}>
                        <OnlineActivityInsightsCard data={data?.insights}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

export default OnlineActivity