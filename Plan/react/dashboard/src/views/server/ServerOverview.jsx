import React from "react";

import {Alert, Card, Col} from "react-bootstrap";
import {
    faExclamationCircle,
    faInfoCircle,
    faPowerOff,
    faTachometerAlt,
    faUser,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../../components/Datapoint";
import {useTranslation} from "react-i18next";
import {useParams} from "react-router";
import {fetchServerOverview} from "../../service/serverService";
import ErrorView from "../ErrorView.tsx";
import {useDataRequest} from "../../hooks/dataFetchHook";
import OnlineActivityCard from "../../components/cards/server/graphs/OnlineActivityCard";
import ServerAsNumbersCard from "../../components/cards/server/values/ServerAsNumbersCard";
import ServerWeekComparisonCard from "../../components/cards/server/tables/ServerWeekComparisonCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {CardLoader} from "../../components/navigation/Loader";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import FormattedTime from "../../components/text/FormattedTime.jsx";

const Last7DaysCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <CardLoader/>;

    const noData = data.average_tps === 'plugin.generic.unavailable'

    return (
        <Card id={"last-7-days"}>
            <Card.Header>
                <h6 className="col-text">
                    {t('html.label.last7days')}
                </h6>
            </Card.Header>
            {noData && <Alert className='alert-warning mb-0'>
                <FontAwesomeIcon icon={faInfoCircle}/> {t('html.description.noData7d')}
            </Alert>}
            <Card.Body>
                <Datapoint name={t('html.label.uniquePlayers')}
                           color={'players-unique'} icon={faUsers}
                           value={data.unique_players} bold/>
                <Datapoint name={t('html.label.uniquePlayers') + ' ' + t('html.label.perDay')}
                           color={'players-unique'} icon={faUser}
                           value={data.unique_players_day} bold/>
                <Datapoint name={t('html.label.newPlayers')}
                           color={'players-new'} icon={faUsers}
                           value={data.new_players} bold/>
                <Datapoint name={t('html.label.newPlayers')}
                           color={'players-new'} icon={faUsers}
                           value={data.new_players_retention_perc}
                           valueLabel={data.new_players_retention + '/' + data.new_players} bold/>
                <hr/>
                <Datapoint name={t('html.label.averageTps')}
                           color={'tps-average'} icon={faTachometerAlt}
                           value={data.average_tps} bold/>
                <Datapoint name={t('html.label.lowTpsSpikes')}
                           color={'tps-low-spikes'} icon={faExclamationCircle}
                           value={data.low_tps_spikes} bold/>
                <Datapoint name={t('html.label.downtime')}
                           color={'downtime'} icon={faPowerOff}
                           value={<FormattedTime timeMs={data.downtime}/>}/>
            </Card.Body>
        </Card>
    )
}

const ServerOverview = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    const seeOverview = hasPermission('page.server.overview.numbers');
    const seeOnlineGraph = hasPermission('page.server.overview.players.online.graph')
    const {data, loadingError} = useDataRequest(
        fetchServerOverview,
        [identifier],
        seeOverview)

    if (loadingError) {
        return <ErrorView error={loadingError}/>
    }

    return (
        <LoadIn>
            <section className="server-overview">
                <ExtendableRow id={'row-server-overview-0'}>
                    {seeOnlineGraph && <Col lg={9}>
                        <OnlineActivityCard/>
                    </Col>}
                    {seeOverview && <Col lg={3}>
                        <Last7DaysCard data={data?.last_7_days}/>
                    </Col>}
                </ExtendableRow>
                {seeOverview && <ExtendableRow id={'row-server-overview-1'}>
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
}

export default ServerOverview;