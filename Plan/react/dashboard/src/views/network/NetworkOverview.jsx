import React from 'react';
import LoadIn from "../../components/animation/LoadIn.tsx";
import {Col} from "react-bootstrap";
import ServerAsNumbersCard from "../../components/cards/server/values/ServerAsNumbersCard";
import ServerWeekComparisonCard from "../../components/cards/server/tables/ServerWeekComparisonCard.tsx";
import NetworkOnlineActivityGraphsCard from "../../components/cards/server/graphs/NetworkOnlineActivityGraphsCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {RecentPlayersCard} from "../../components/cards/network/RecentPlayersCard.tsx";

const NetworkOverview = () => {
    const {hasPermission, hasChildPermission} = useAuth();
    const seeOverview = hasPermission('page.network.overview.numbers');
    const seeGraphs = hasChildPermission('page.network.overview.graphs');

    return (
        <LoadIn>
            <section className="network_overview">
                <ExtendableRow id={'row-network-overview-0'}>
                    {seeGraphs && <Col lg={seeOverview ? 9 : 12}>
                        <NetworkOnlineActivityGraphsCard/>
                    </Col>}
                    {seeOverview && <Col lg={3}>
                        <RecentPlayersCard/>
                    </Col>}
                </ExtendableRow>
                {seeOverview && <ExtendableRow id={'row-network-overview-1'}>
                    <Col lg={4}>
                        <ServerAsNumbersCard/>
                    </Col>
                    <Col lg={8}>
                        <ServerWeekComparisonCard/>
                    </Col>
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
};

export default NetworkOverview