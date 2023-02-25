import React from "react";
import {Col} from "react-bootstrap";
import OnlineActivityGraphsCard from "../../components/cards/server/graphs/OnlineActivityGraphsCard";
import OnlineActivityAsNumbersCard from "../../components/cards/server/tables/OnlineActivityAsNumbersCard";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchOnlineActivityOverview} from "../../service/serverService";
import ErrorView from "../ErrorView";
import OnlineActivityInsightsCard from "../../components/cards/server/insights/OnlineActivityInsightsCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const OnlineActivity = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchOnlineActivityOverview, [identifier])

    if (loadingError) return <ErrorView error={loadingError}/>

    return (
        <LoadIn>
            <section className="server-online-activity-overview">
                <ExtendableRow id={'row-server-online-activity-overview-0'}>
                    <Col lg={12}>
                        <OnlineActivityGraphsCard/>
                    </Col>
                </ExtendableRow>
                <ExtendableRow id={'row-server-online-activity-overview-1'}>
                    <Col lg={8}>
                        <OnlineActivityAsNumbersCard data={data?.numbers}/>
                    </Col>
                    <Col lg={4}>
                        <OnlineActivityInsightsCard data={data?.insights}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
}

export default OnlineActivity