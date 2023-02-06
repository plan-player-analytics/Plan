import React from "react";
import PvpPveAsNumbersCard from "../../components/cards/server/tables/PvpPveAsNumbersCard";
import {Col, Row} from "react-bootstrap";
import PvpKillsTableCard from "../../components/cards/common/PvpKillsTableCard";
import PvpPveInsightsCard from "../../components/cards/server/insights/PvpPveInsightsCard";
import {useParams} from "react-router-dom";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchKills, fetchPvpPve} from "../../service/serverService";
import ErrorView from "../ErrorView";
import LoadIn from "../../components/animation/LoadIn";

const ServerPvpPve = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(fetchPvpPve, [identifier]);
    const {data: killsData, loadingError: killsLoadingError} = useDataRequest(fetchKills, [identifier]);

    if (loadingError) return <ErrorView error={loadingError}/>
    if (killsLoadingError) return <ErrorView error={killsLoadingError}/>

    return (
        <LoadIn show={data && killsData}>
            <section className="server_pvp_pve">
                <Row>
                    <Col lg={8}>
                        <PvpPveAsNumbersCard kill_data={data?.numbers}/>
                    </Col>
                    <Col lg={4}>
                        <PvpPveInsightsCard data={data?.insights}/>
                    </Col>
                </Row>
                <Row>
                    <Col lg={8}>
                        <PvpKillsTableCard player_kills={killsData?.player_kills}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

export default ServerPvpPve;