import React, {useCallback} from 'react';
import PerformanceAsNumbersTable from "../../../table/PerformanceAsNumbersTable";
import CardHeader from "../../CardHeader";
import {faBookOpen} from "@fortawesome/free-solid-svg-icons";
import {Card} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {DatapointType} from "../../../../dataHooks/model/datapoint/Datapoint";
import {MS_24H, MS_MONTH, MS_WEEK} from "../../../../util/format/useDateFormatter";
import {useNavigation} from "../../../../hooks/navigationHook";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import {useDatapointQuery} from "../../../../dataHooks/useDatapointQuery";
import {Server} from "../../../../hooks/model/Metadata";

type NoDataProps = {
    servers: string[];
}

const NoDataAlert = ({servers}: NoDataProps) => {
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

type Props = {
    servers: Server[];
}

const PerformanceAsNumbersCard = ({servers}: Props) => {
    const {setHelpModalTopic} = useNavigation();
    const openHelp = useCallback(() => setHelpModalTopic('performance'), [setHelpModalTopic]);

    return (
        <Card id={"performance-as-numbers"}>
            <CardHeader icon={faBookOpen} color="chunks" label={"html.label.performanceAsNumbers"}>
                <button className={"float-end"} onClick={openHelp}>
                    <Fa className={"col-help-icon"}
                        icon={faQuestionCircle}/>
                </button>
            </CardHeader>
            <NoDataAlert servers={servers.map(s => s.serverUUID)}/>
            <PerformanceAsNumbersTable servers={servers}/>
        </Card>
    )
};

export default PerformanceAsNumbersCard