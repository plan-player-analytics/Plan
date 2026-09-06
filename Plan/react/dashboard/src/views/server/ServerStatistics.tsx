import React from "react";
import {Col} from "react-bootstrap";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import {StatisticsTable} from "../../components/table/StatisticsTable";
import {useParams} from "react-router";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchStatistics} from "../../service/serverService";
import {PlanDataResponse} from "../../service/PlanResponse";
import {ErrorViewCard} from "../ErrorView";
import {CardLoader} from "../../components/navigation/Loader";
import {ServerStatistics as ServerStatisticsType} from "../../dataHooks/model/ServerStatistics";

const ServerStatistics = () => {
    const {hasPermission} = useAuth();
    const {identifier} = useParams();

    return (
        <LoadIn>
            {hasPermission('page.server.statistics') && <section className="server-statistics" id={"server-statistics"}>
                <ExtendableRow id={'row-server-statistics-0'}>
                    <Col lg={12}>
                        <LoadingStatistics identifier={identifier}/>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
}

type Props = {
    identifier?: string;
}

const LoadingStatistics = ({identifier}: Props) => {
    const {
        data,
        loadingError
    } = useDataRequest(fetchStatistics, [identifier]) as unknown as PlanDataResponse<ServerStatisticsType[]>;

    if (loadingError) return <ErrorViewCard error={loadingError}/>
    if (!data) return <CardLoader/>;
    return <StatisticsTable statistics={data} showAllByDefault/>
}

export default ServerStatistics;