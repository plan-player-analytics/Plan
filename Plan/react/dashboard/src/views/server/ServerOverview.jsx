import React from "react";

import {Alert, Card, Col} from "react-bootstrap";
import {
    faExclamationCircle,
    faInfoCircle,
    faPowerOff,
    faTachometerAlt,
    faUser,
    faUserCircle,
    faUsers
} from "@fortawesome/free-solid-svg-icons";
import {useTranslation} from "react-i18next";
import {useParams} from "react-router";
import OnlineActivityCard from "../../components/cards/server/graphs/OnlineActivityCard";
import ServerAsNumbersCard from "../../components/cards/server/values/ServerAsNumbersCard";
import ServerWeekComparisonCard from "../../components/cards/server/tables/ServerWeekComparisonCard.tsx";
import LoadIn from "../../components/animation/LoadIn.tsx";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {QueryDatapoint, useDatapointQuery} from "../../components/datapoint/QueryDatapoint.tsx";
import {DatapointType} from "../../dataHooks/model/datapoint/Datapoint.ts";
import {MS_WEEK} from "../../util/format/useDateFormatter.js";
import {GenericFilterContextProvider} from "../../dataHooks/genericFilterContextHook.tsx";

const Last7DaysCard = () => {
    const {t} = useTranslation();
    const {identifier} = useParams();

    const {error} = useDatapointQuery(true, DatapointType.TPS_AVERAGE, {server: identifier, afterMillisAgo: MS_WEEK})
    const noData = error?.status === 404

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
            <GenericFilterContextProvider initialValue={{server: identifier, afterMillisAgo: MS_WEEK}}>
                {filter => (
                    <Card.Body>
                        <QueryDatapoint name={t('html.label.uniquePlayers')}
                                        color={'players-unique'} icon={faUsers}
                                        dataType={DatapointType.UNIQUE_PLAYERS_COUNT}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.uniquePlayers') + ' ' + t('html.label.perDay')}
                                        color={'players-unique'} icon={faUser}
                                        dataType={DatapointType.UNIQUE_PLAYERS_AVERAGE}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.newPlayers')}
                                        color={'players-new'} icon={faUsers}
                                        dataType={DatapointType.NEW_PLAYERS}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.newPlayerRetention')}
                                        color={'players-new'} icon={faUserCircle}
                                        dataType={DatapointType.NEW_PLAYER_RETENTION}
                                        filter={filter} bold/>
                        <hr/>
                        <QueryDatapoint name={t('html.label.averageTps')}
                                        color={'tps-average'} icon={faTachometerAlt}
                                        dataType={DatapointType.TPS_AVERAGE}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.lowTpsSpikes')}
                                        color={'tps-low-spikes'} icon={faExclamationCircle}
                                        dataType={DatapointType.TPS_LOW_SPIKES}
                                        filter={filter} bold/>
                        <QueryDatapoint name={t('html.label.downtime')}
                                        color={'downtime'} icon={faPowerOff}
                                        dataType={DatapointType.DOWNTIME}
                                        filter={filter}/>
                    </Card.Body>
                )}
            </GenericFilterContextProvider>
        </Card>
    )
}

const ServerOverview = () => {
    const {hasPermission} = useAuth();

    const seeOverview = hasPermission('page.server.overview.numbers');
    const seeOnlineGraph = hasPermission('page.server.overview.players.online.graph')

    return (
        <LoadIn>
            <section className="server-overview">
                <ExtendableRow id={'row-server-overview-0'}>
                    {seeOnlineGraph && <Col lg={9}>
                        <OnlineActivityCard/>
                    </Col>}
                    {seeOverview && <Col lg={3}>
                        <Last7DaysCard/>
                    </Col>}
                </ExtendableRow>
                {seeOverview && <ExtendableRow id={'row-server-overview-1'}>
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
}

export default ServerOverview;