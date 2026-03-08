import React from 'react';
import PerformanceAsNumbersTable from "../../../table/PerformanceAsNumbersTable";
import CardHeader from "../../CardHeader.tsx";
import {faBookOpen, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {Alert, Card} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const NoDataAlert = ({data}) => {
    const {t} = useTranslation();
    const noData24h = data?.cpu_24h === "plugin.generic.unavailable";
    const noData7d = data?.cpu_7d === "plugin.generic.unavailable";
    const noData30d = data?.cpu_30d === "plugin.generic.unavailable";

    if (noData30d) return <p className={"alert alert-warning mb-0"}>{t('html.description.noData30d')}</p>;
    if (noData7d) return <p className={"alert alert-warning mb-0"}>{t('html.description.noData7d')}</p>;
    if (noData24h) return <p className={"alert alert-warning mb-0"}>{t('html.description.noData24h')}</p>;
    return null;
}

const PerformanceAsNumbersCard = ({data, servers}) => {
    const {t} = useTranslation();
    const dataIncludesGameServers = !servers || Boolean(servers.filter(server => !server.proxy).length);

    return (
        <Card id={"performance-as-numbers"}>
            <CardHeader icon={faBookOpen} color="chunks" label={"html.label.performanceAsNumbers"}/>
            <NoDataAlert data={data}/>
            {!dataIncludesGameServers && <Alert className='alert-warning mb-0'>
                <FontAwesomeIcon icon={faInfoCircle}/> {t('html.description.performanceNoGameServers')}
            </Alert>}
            <PerformanceAsNumbersTable data={data} servers={servers}/>
        </Card>
    )
};

export default PerformanceAsNumbersCard