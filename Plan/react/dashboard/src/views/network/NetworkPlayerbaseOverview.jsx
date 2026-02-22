import {Col} from "react-bootstrap";
import React from "react";
import PlayerbaseDevelopmentCard from "../../components/cards/server/graphs/PlayerbaseDevelopmentCard";
import CurrentPlayerbaseCard from "../../components/cards/server/graphs/CurrentPlayerbaseCard";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {ErrorViewCard} from "../ErrorView.tsx";
import PlayerbaseTrendsCard from "../../components/cards/server/tables/PlayerbaseTrendsCard";
import PlayerbaseInsightsCard from "../../components/cards/server/insights/PlayerbaseInsightsCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {fetchNetworkPlayerbaseOverview} from "../../service/networkService";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const NetworkPlayerbaseOverview = () => {
    const {hasPermission} = useAuth();

    const seeOverview = hasPermission('page.network.playerbase.overview');
    const seeGraphs = hasPermission('page.network.playerbase.graphs');
    const {data, loadingError} = useDataRequest(fetchNetworkPlayerbaseOverview, [], seeOverview);

    return (
        <LoadIn>
            <section className="network-playerbase">
                {seeGraphs && <ExtendableRow id={'row-network-playerbase-0'}>
                    <Col lg={8}>
                        <PlayerbaseDevelopmentCard identifier={undefined}/>
                    </Col>
                    <Col lg={4}>
                        <CurrentPlayerbaseCard identifier={undefined}/>
                    </Col>
                </ExtendableRow>}
                {seeOverview && <ExtendableRow id={'row-network-playerbase-1'}>
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

export default NetworkPlayerbaseOverview;