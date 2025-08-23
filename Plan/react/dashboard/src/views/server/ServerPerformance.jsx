import React from 'react';
import LoadIn from "../../components/animation/LoadIn";
import {Col} from "react-bootstrap";
import {useParams} from "react-router";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPerformanceOverview} from "../../service/serverService";
import PerformanceGraphsCard from "../../components/cards/server/graphs/PerformanceGraphsCard";
import PerformanceInsightsCard from "../../components/cards/server/insights/PerformanceInsightsCard";
import {ErrorViewCard} from "../ErrorView";
import PerformanceAsNumbersCard from "../../components/cards/server/tables/PerformanceAsNumbersCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const ServerPerformance = () => {
    const {hasPermission, hasChildPermission} = useAuth();
    const {identifier} = useParams();

    const seeGraphs = hasChildPermission('page.server.performance.graphs');
    const seeOverview = hasPermission('page.server.performance.overview');
    const {data, loadingError} = useDataRequest(fetchPerformanceOverview, [identifier], seeOverview);

    return (
        <LoadIn>
            <section className="server-performance">
                {seeGraphs && <ExtendableRow id={'row-server-performance-0'}>
                    <Col lg={12}>
                        <PerformanceGraphsCard/>
                    </Col>
                </ExtendableRow>}
                {seeOverview && <ExtendableRow id={'row-server-performance-1'}>
                    <Col lg={8}>
                        {loadingError ? <ErrorViewCard error={loadingError}/> :
                            <PerformanceAsNumbersCard data={data?.numbers}/>}
                    </Col>
                    <Col lg={4}>
                        {loadingError ? <ErrorViewCard error={loadingError}/> :
                            <PerformanceInsightsCard data={data?.insights}/>}
                    </Col>
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
};

export default ServerPerformance