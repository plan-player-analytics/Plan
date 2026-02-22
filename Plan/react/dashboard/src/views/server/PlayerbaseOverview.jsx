import {Col} from "react-bootstrap";
import React from "react";
import PlayerbaseDevelopmentCard from "../../components/cards/server/graphs/PlayerbaseDevelopmentCard";
import CurrentPlayerbaseCard from "../../components/cards/server/graphs/CurrentPlayerbaseCard";
import {useParams} from "react-router";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPlayerbaseOverview} from "../../service/serverService";
import {ErrorViewCard} from "../ErrorView.tsx";
import PlayerbaseTrendsCard from "../../components/cards/server/tables/PlayerbaseTrendsCard";
import PlayerbaseInsightsCard from "../../components/cards/server/insights/PlayerbaseInsightsCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const PlayerbaseOverview = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeOverview = hasPermission('page.server.playerbase.overview');
    const seeGraphs = hasPermission('page.server.playerbase.graphs');
    const {data, loadingError} = useDataRequest(fetchPlayerbaseOverview, [identifier], seeOverview);

    return (
        <LoadIn>
            <section className="server-playerbase">
                {seeGraphs && <ExtendableRow id={'row-server-playerbase-0'}>
                    <Col lg={8}>
                        <PlayerbaseDevelopmentCard identifier={identifier}/>
                    </Col>
                    <Col lg={4}>
                        <CurrentPlayerbaseCard identifier={identifier}/>
                    </Col>
                </ExtendableRow>}
                {seeOverview && <ExtendableRow id={'row-server-playerbase-1'}>
                    {loadingError && <ErrorViewCard error={loadingError}/>}
                    {!loadingError && <>
                        <Col lg={8}>
                            <PlayerbaseTrendsCard data={data?.trends}/>
                        </Col>
                        <Col lg={4}>
                            <PlayerbaseInsightsCard data={data?.insights}/>
                        </Col>
                    </>}
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
}

export default PlayerbaseOverview;