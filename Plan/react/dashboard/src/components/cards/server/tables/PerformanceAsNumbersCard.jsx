import React from 'react';
import PerformanceAsNumbersTable from "../../../table/PerformanceAsNumbersTable";
import CardHeader from "../../CardHeader.tsx";
import {faBookOpen} from "@fortawesome/free-solid-svg-icons";
import {Card} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {useDatapointQuery} from "../../../datapoint/QueryDatapoint.tsx";
import {DatapointType} from "../../../../dataHooks/model/datapoint/Datapoint.ts";
import {MS_24H, MS_MONTH, MS_WEEK} from "../../../../util/format/useDateFormatter.js";

const NoDataAlert = ({servers}) => {
    const {t} = useTranslation();
    const {error: error24h} = useDatapointQuery(true, DatapointType.CPU_AVERAGE, {
        server: servers,
        afterMillisAgo: MS_24H
    })
    const noData24h = error24h?.status === 404
    const {error: error7d} = useDatapointQuery(true, DatapointType.CPU_AVERAGE, {
        server: servers,
        afterMillisAgo: MS_WEEK
    })
    const noData7d = error7d?.status === 404
    const {error: error30d} = useDatapointQuery(true, DatapointType.CPU_AVERAGE, {
        server: servers,
        afterMillisAgo: MS_MONTH
    })
    const noData30d = error30d?.status === 404;

    if (noData30d) return <p className={"alert alert-warning mb-0"}>{t('html.description.noData30d')}</p>;
    if (noData7d) return <p className={"alert alert-warning mb-0"}>{t('html.description.noData7d')}</p>;
    if (noData24h) return <p className={"alert alert-warning mb-0"}>{t('html.description.noData24h')}</p>;
    return null;
}

const PerformanceAsNumbersCard = ({servers}) => {
    return (
        <Card id={"performance-as-numbers"}>
            <CardHeader icon={faBookOpen} color="chunks" label={"html.label.performanceAsNumbers"}/>
            <NoDataAlert servers={servers.map(s => s.serverUUID)}/>
            <PerformanceAsNumbersTable servers={servers}/>
        </Card>
    )
};

export default PerformanceAsNumbersCard