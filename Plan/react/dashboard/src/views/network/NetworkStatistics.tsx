import React from "react";
import {Col} from "react-bootstrap";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import {StatisticsTable} from "../../components/table/StatisticsTable";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {PlanDataResponse} from "../../service/PlanResponse";
import {CardLoader} from "../../components/navigation/Loader";
import {ServerStatistics as ServerStatisticsType} from "../../dataHooks/model/ServerStatistics";
import {fetchNetworkStatistics} from "../../service/networkService";
import {useTranslation} from "react-i18next";
import {faArrowUpRightDots} from "@fortawesome/free-solid-svg-icons";
import CardTabs from "../../components/CardTabs";
import {ErrorViewCard} from "../ErrorView";

const NetworkStatistics = () => {
    const {hasPermission} = useAuth();

    return (
        <LoadIn>
            {hasPermission('page.network.statistics') &&
                <section className="network-statistics" id={"network-statistics"}>
                    <ExtendableRow id={'row-network-statistics-0'}>
                        <Col lg={12}>
                            <StatisticsTabs/>
                        </Col>
                    </ExtendableRow>
                </section>}
        </LoadIn>
    )
}

const StatisticsTabs = () => {
    const {hasPermission} = useAuth();
    const {t} = useTranslation();
    const tabs = [
        {
            name: t('html.label.total'), icon: faArrowUpRightDots,
            href: 'aggregated',
            element: <LoadingAggregateStatistics/>,
            permission: 'page.network.statistics.aggregate'
        }, {
            name: t('html.label.servers'),
            icon: faArrowUpRightDots,
            href: 'byServer',
            element: <LoadingStatistics/>,
            permission: 'page.network.statistics.byServer'
        },
    ].filter(tab => hasPermission(tab.permission));
    return <CardTabs tabs={tabs}/>
}

const LoadingStatistics = () => {
    const {
        data,
        loadingError
    } = useDataRequest(fetchNetworkStatistics, [false]) as unknown as PlanDataResponse<ServerStatisticsType[]>;

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;
    return <div id={"network-aggregate-statistics"}>
        <StatisticsTable statistics={data}/>
    </div>
}

const LoadingAggregateStatistics = () => {
    const {
        data,
        loadingError
    } = useDataRequest(fetchNetworkStatistics, [true]) as unknown as PlanDataResponse<ServerStatisticsType[]>;

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;
    return <div id={"network-by-server-statistics"}>
        <StatisticsTable statistics={data} showAllByDefault/>
    </div>
}

export default NetworkStatistics;