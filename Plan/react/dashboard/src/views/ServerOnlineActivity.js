import React from "react";
import {Col, Row} from "react-bootstrap-v5";
import OnlineActivityGraphsCard from "../components/cards/server/OnlineActivityGraphsCard";
import OnlineActivityAsNumbersCard from "../components/cards/server/OnlineActivityAsNumbersCard";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../hooks/dataFetchHook";
import {fetchOnlineActivityOverview} from "../service/serverService";
import {ErrorViewBody} from "./ErrorView";
import OnlineActivityInsightsCard from "../components/cards/server/OnlineActivityInsightsCard";

const ServerOnlineActivity = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchOnlineActivityOverview, [identifier])

    if (loadingError) return <ErrorViewBody error={loadingError}/>
    if (!data) return <></>;

    return (
        <section className="server_online_activity_overview">
            <Row>
                <Col lg={12}>
                    <OnlineActivityGraphsCard/>
                </Col>
            </Row>
            <Row>
                <Col lg={8}>
                    <OnlineActivityAsNumbersCard data={data}/>
                </Col>
                <Col lg={4}>
                    <OnlineActivityInsightsCard data={data}/>
                </Col>
            </Row>
        </section>
    )
}

export default ServerOnlineActivity