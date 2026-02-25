import React from "react";
import PvpPveAsNumbersCard from "../../components/cards/server/tables/PvpPveAsNumbersCard";
import {Col} from "react-bootstrap";
import PvpKillsTableCard from "../../components/cards/common/PvpKillsTableCard";
import PvpPveInsightsCard from "../../components/cards/server/insights/PvpPveInsightsCard";
import {useParams} from "react-router";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchKills, fetchPvpPve} from "../../service/serverService";
import ErrorView from "../ErrorView.tsx";
import LoadIn from "../../components/animation/LoadIn.tsx";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";

const ServerPvpPve = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeKillNumbers = hasPermission('page.server.player.versus.overview');
    const seeKills = hasPermission('page.server.player.versus.kill.list');

    const {data, loadingError} = useDataRequest(fetchPvpPve, [identifier], seeKillNumbers);
    const {data: killsData, loadingError: killsLoadingError} = useDataRequest(fetchKills, [identifier], seeKills);

    if (loadingError) return <ErrorView error={loadingError}/>
    if (killsLoadingError) return <ErrorView error={killsLoadingError}/>

    return (
        <LoadIn>
            <section className="server-pvp-pve">
                {seeKillNumbers && <ExtendableRow id={'row-server-pvp-pve-0'}>
                    <Col lg={8}>
                        <PvpPveAsNumbersCard kill_data={data?.numbers}/>
                    </Col>
                    <Col lg={4}>
                        <PvpPveInsightsCard data={data?.insights}/>
                    </Col>
                </ExtendableRow>}
                {seeKills && <ExtendableRow id={'row-server-pvp-pve-1'}>
                    <Col lg={8}>
                        <PvpKillsTableCard player_kills={killsData?.player_kills}/>
                    </Col>
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
}

export default ServerPvpPve;