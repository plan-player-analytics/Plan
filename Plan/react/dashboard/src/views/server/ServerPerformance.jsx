import React from 'react';
import LoadIn from "../../components/animation/LoadIn.tsx";
import {Col} from "react-bootstrap";
import {useParams} from "react-router";
import PerformanceGraphsCard from "../../components/cards/server/graphs/PerformanceGraphsCard";
import PerformanceAsNumbersCard from "../../components/cards/server/tables/PerformanceAsNumbersCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const ServerPerformance = () => {
    const {hasPermission, hasChildPermission} = useAuth();
    const {identifier} = useParams();

    const seeGraphs = hasChildPermission('page.server.performance.graphs');
    const seeOverview = hasPermission('page.server.performance.overview');

    return (
        <LoadIn>
            <section className="server-performance">
                {seeGraphs && <ExtendableRow id={'row-server-performance-0'}>
                    <Col lg={12}>
                        <PerformanceGraphsCard/>
                    </Col>
                </ExtendableRow>}
                {seeOverview && <ExtendableRow id={'row-server-performance-1'}>
                    <Col lg={12}>
                        <PerformanceAsNumbersCard servers={[{serverUUID: identifier}]}/>
                    </Col>
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
};

export default ServerPerformance