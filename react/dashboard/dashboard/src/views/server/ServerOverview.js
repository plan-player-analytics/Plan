import React from "react";

import {Card, Col} from "react-bootstrap";
import {faExclamationCircle, faPowerOff, faTachometerAlt, faUser, faUsers} from "@fortawesome/free-solid-svg-icons";
import Datapoint from "../../components/Datapoint";
import {useTranslation} from "react-i18next";
import {useParams} from "react-router-dom";
import {fetchServerOverview} from "../../service/serverService";
import ErrorView from "../ErrorView";
import {useDataRequest} from "../../hooks/dataFetchHook";
import OnlineActivityCard from "../../components/cards/server/graphs/OnlineActivityCard";
import ServerAsNumbersCard from "../../components/cards/server/values/ServerAsNumbersCard";
import ServerWeekComparisonCard from "../../components/cards/server/tables/ServerWeekComparisonCard";
import LoadIn from "../../components/animation/LoadIn";
import {CardLoader} from "../../components/navigation/Loader";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";

const Last7DaysCard = ({data}) => {
    const {t} = useTranslation();

    if (!data) return <CardLoader/>;

    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    {t('html.label.last7days')}
                </h6>
            </Card.Header>
            <Card.Body>
                <Datapoint name={t('html.label.uniquePlayers')}
                           color={'blue'} icon={faUsers}
                           value={data.unique_players} bold/>
                <Datapoint name={t('html.label.uniquePlayers') + ' ' + t('html.label.perDay')}
                           color={'blue'} icon={faUser}
                           value={data.unique_players_day} bold/>
                <Datapoint name={t('html.label.newPlayers')}
                           color={'light-green'} icon={faUsers}
                           value={data.new_players} bold/>
                <Datapoint name={t('html.label.newPlayers')}
                           color={'light-green'} icon={faUsers}
                           value={data.new_players_retention_perc}
                           valueLabel={data.new_players_retention + '/' + data.new_players} bold/>
                <hr/>
                <Datapoint name={t('html.label.averageTps')}
                           color={'orange'} icon={faTachometerAlt}
                           value={data.average_tps} bold/>
                <Datapoint name={t('html.label.lowTpsSpikes')}
                           color={'red'} icon={faExclamationCircle}
                           value={data.low_tps_spikes} bold/>
                <Datapoint name={t('html.label.downtime')}
                           color={'red'} icon={faPowerOff}
                           value={data.downtime}/>
            </Card.Body>
        </Card>
    )
}

const ServerOverview = () => {
    const {identifier} = useParams();

    const {data, loadingError} = useDataRequest(
        fetchServerOverview,
        [identifier])

    if (loadingError) {
        return <ErrorView error={loadingError}/>
    }

    return (
        <LoadIn>
            <section className="server-overview">
                <ExtendableRow id={'row-server-overview-0'}>
                    <Col lg={9}>
                        <OnlineActivityCard/>
                    </Col>
                    <Col lg={3}>
                        <Last7DaysCard data={data?.last_7_days}/>
                    </Col>
                </ExtendableRow>
                <ExtendableRow id={'row-server-overview-1'}>
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
}

export default ServerOverview;