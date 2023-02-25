import React from 'react';
import {useDataRequest} from "../../hooks/dataFetchHook";
import ErrorView from "../ErrorView";
import LoadIn from "../../components/animation/LoadIn";
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


const RecentPlayersCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <CardLoader/>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    {t('html.label.players')}
                </h6>
            </Card.Header>
            <ExtendableCardBody id={'card-body-network-overview-players'}>
                <p>{t('html.label.last24hours')}</p>
                <Datapoint icon={faUsers} color="light-blue"
                           name={t('html.label.uniquePlayers')} value={data.unique_players_1d}/>
                <Datapoint icon={faUsers} color="light-green"
                           name={t('html.label.newPlayers')} value={data.new_players_1d}/>
                <p>{t('html.label.last7days')}</p>
                <Datapoint icon={faUsers} color="light-blue"
                           name={t('html.label.uniquePlayers')} value={data.unique_players_7d}/>
                <Datapoint icon={faUsers} color="light-green"
                           name={t('html.label.newPlayers')} value={data.new_players_7d}/>
                <p>{t('html.label.last30days')}</p>
                <Datapoint icon={faUsers} color="light-blue"
                           name={t('html.label.uniquePlayers')} value={data.unique_players_30d}/>
                <Datapoint icon={faUsers} color="light-green"
                           name={t('html.label.newPlayers')} value={data.new_players_30d}/>
            </ExtendableCardBody>
        </Card>
    )
}

const NetworkOverview = () => {
    const {data, loadingError} = useDataRequest(fetchNetworkOverview, [])

    if (loadingError) {
        return <ErrorView error={loadingError}/>
    }

    return (
        <LoadIn>
            <section className="network_overview">
                <ExtendableRow id={'row-network-overview-0'}>
                    <Col lg={9}>
                        <NetworkOnlineActivityGraphsCard/>
                    </Col>
                    <Col lg={3}>
                        <RecentPlayersCard data={data?.players}/>
                    </Col>
                </ExtendableRow>
                <ExtendableRow id={'row-network-overview-1'}>
                    <Col lg={4}>
                        <ServerAsNumbersCard data={data?.numbers}/>
                    </Col>
                    <Col lg={8}>
                        <ServerWeekComparisonCard data={data?.weeks}/>
                    </Col>
                </ExtendableRow>
            </section>
        </LoadIn>
    )
};

export default NetworkOverview