import React from 'react';
import LoadIn from "../../components/animation/LoadIn";
import {Col, Row} from "react-bootstrap-v5";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPerformanceOverview} from "../../service/serverService";
import PerformanceGraphsCard from "../../components/cards/server/graphs/PerformanceGraphsCard";
import PerformanceInsightsCard from "../../components/cards/server/insights/PerformanceInsightsCard";
import {ErrorViewCard} from "../ErrorView";
import PerformanceAsNumbersCard from "../../components/cards/server/tables/PerformanceAsNumbersCard";

const ServerPerformance = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPerformanceOverview, [identifier]);

    return (
        <LoadIn>
            <section className="server_performance">
                <Row>
                    <Col lg={12}>
                        <PerformanceGraphsCard/>
                    </Col>
                </Row>
                <Row>
                    <Col lg={8}>
                        {loadingError ? <ErrorViewCard error={loadingError}/> :
                            <PerformanceAsNumbersCard data={data?.numbers}/>}
                    </Col>
                    <Col lg={4}>
                        {loadingError ? <ErrorViewCard error={loadingError}/> :
                            <PerformanceInsightsCard data={data?.insights}/>}
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
};

export default ServerPerformance