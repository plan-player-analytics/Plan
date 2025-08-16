import React from 'react';
import PerformanceAsNumbersTable from "../../../table/PerformanceAsNumbersTable";
import CardHeader from "../../CardHeader";
import {faBookOpen, faInfoCircle} from "@fortawesome/free-solid-svg-icons";
import {Alert, Card} from "react-bootstrap";
import {useTranslation} from "react-i18next";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";

const PerformanceAsNumbersCard = ({data, servers}) => {
    const {t} = useTranslation();
    const noData24h = data && "Unavailable" === data.cpu_24h;
    const noData7d = data && "Unavailable" === data.cpu_7d;
    const noData30d = data && "Unavailable" === data.cpu_30d;

    const noDataAlert = noData30d ? <p className={"alert alert-warning mb-0"}>{t('html.description.noData30d')}</p>
        : (noData7d ? <p className={"alert alert-warning mb-0"}>{t('html.description.noData7d')}</p>
            : (noData24h ? <p className={"alert alert-warning mb-0"}>{t('html.description.noData24h')}</p>
                : ''));
    const dataIncludesGameServers = !servers || Boolean(servers.filter(server => !server.proxy).length);

    return (
        <Card id={"performance-as-numbers"}>
            <CardHeader icon={faBookOpen} color="chunks" label={"html.label.performanceAsNumbers"}/>
            {noDataAlert}
            {!dataIncludesGameServers && <Alert className='alert-warning mb-0'>
                <FontAwesomeIcon icon={faInfoCircle}/> {t('html.description.performanceNoGameServers')}
            </Alert>}
            <PerformanceAsNumbersTable data={data} servers={servers}/>
        </Card>
    )
};

export default PerformanceAsNumbersCard