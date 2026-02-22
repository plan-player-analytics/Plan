import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import ErrorView from "../ErrorView.tsx";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {Card, Col} from "react-bootstrap";
import ServerAsNumbersCard from "../../components/cards/server/values/ServerAsNumbersCard";
import ServerWeekComparisonCard from "../../components/cards/server/tables/ServerWeekComparisonCard";
import {fetchNetworkOverview} from "../../service/networkService";
import {useTranslation} from "react-i18next";
import {CardLoader} from "../../components/navigation/Loader";
import Datapoint from "../../components/Datapoint";
import {faUsers} from "@fortawesome/free-solid-svg-icons";
import NetworkOnlineActivityGraphsCard from "../../components/cards/server/graphs/NetworkOnlineActivityGraphsCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import ExtendableCardBody from "../../components/layout/extension/ExtendableCardBody";
import {useAuth} from "../../hooks/authenticationHook";


const RecentPlayersCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <CardLoader/>;

    return (
        <Card id={"recent-players"}>
            <Card.Header>
                <h6 className="col-text">
                    {t('html.label.players')}
                </h6>
            </Card.Header>
            <ExtendableCardBody id={'card-body-network-overview-players'}>
                <p>{t('html.label.last24hours')}</p>
                <Datapoint icon={faUsers} color="players-unique"
                           name={t('html.label.uniquePlayers')} value={data.unique_players_1d}/>
                <Datapoint icon={faUsers} color="players-new"
                           name={t('html.label.newPlayers')} value={data.new_players_1d}/>
                <p>{t('html.label.last7days')}</p>
                <Datapoint icon={faUsers} color="players-unique"
                           name={t('html.label.uniquePlayers')} value={data.unique_players_7d}/>
                <Datapoint icon={faUsers} color="players-new"
                           name={t('html.label.newPlayers')} value={data.new_players_7d}/>
                <p>{t('html.label.last30days')}</p>
                <Datapoint icon={faUsers} color="players-unique"
                           name={t('html.label.uniquePlayers')} value={data.unique_players_30d}/>
                <Datapoint icon={faUsers} color="players-new"
                           name={t('html.label.newPlayers')} value={data.new_players_30d}/>
            </ExtendableCardBody>
        </Card>
    )
}

const NetworkOverview = () => {
    const {hasPermission, hasChildPermission} = useAuth();
    const seeOverview = hasPermission('page.network.overview.numbers');
    const seeGraphs = hasChildPermission('page.network.overview.graphs');
    const {data, loadingError} = useDataRequest(fetchNetworkOverview, [], seeOverview)

    if (loadingError) {
        return <ErrorView error={loadingError}/>
    }

    return (
        <LoadIn>
            <section className="network_overview">
                <ExtendableRow id={'row-network-overview-0'}>
                    {seeGraphs && <Col lg={seeOverview ? 9 : 12}>
                        <NetworkOnlineActivityGraphsCard/>
                    </Col>}
                    {seeOverview && <Col lg={3}>
                        <RecentPlayersCard data={data?.players}/>
                    </Col>}
                </ExtendableRow>
                {seeOverview && <ExtendableRow id={'row-network-overview-1'}>
                    <Col lg={4}>
                        <ServerAsNumbersCard data={data?.numbers}/>
                    </Col>
                    <Col lg={8}>
                        <ServerWeekComparisonCard data={data?.weeks}/>
                    </Col>
                </ExtendableRow>}
            </section>
        </LoadIn>
    )
};

export default NetworkOverview