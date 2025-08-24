import {useTranslation} from "react-i18next";
import {
    faDragon,
    faExclamationCircle,
    faHdd,
    faMap,
    faMicrochip,
    faPowerOff,
    faTachometerAlt,
    faUser
} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {TableRow} from "./TableRow";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faEye, faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import AsNumbersTable from "./AsNumbersTable";
import {ChartLoader} from "../navigation/Loader";
import FormattedTime from "../text/FormattedTime.jsx";

const PerformanceAsNumbersTable = ({data, servers}) => {
    const {t} = useTranslation();
    if (!data) return <ChartLoader/>;

    const dataIncludesGameServers = servers && Boolean(servers.filter(server => !server.proxy).length);
    const noTPSOnProxies = !servers || dataIncludesGameServers
        ? ''
        : <span title={t('html.description.performanceNoGameServers')}><Fa icon={faQuestionCircle}/></span>;

    return (
        <AsNumbersTable
            headers={[t('html.label.last30days'), t('html.label.last7days'), t('html.label.last24hours')]}
        >
            <TableRow icon={faExclamationCircle} color="tps-low-spikes" text={t('html.label.lowTpsSpikes')}
                      values={[
                          data.low_tps_spikes_30d,
                          data.low_tps_spikes_7d,
                          data.low_tps_spikes_24h
                      ]}/>
            <TableRow icon={faPowerOff} color="downtime"
                      text={t(data.avg_server_downtime_30d ? 'html.label.serverDowntime' : 'html.label.totalServerDowntime') + ' (' + t('generic.noData') + ')'}
                      values={[
                          <FormattedTime timeMs={data.server_downtime_30d}/>,
                          <FormattedTime timeMs={data.server_downtime_7d}/>,
                          <FormattedTime timeMs={data.server_downtime_24h}/>
                      ]}/>
            {data.avg_server_downtime_30d && (
                <TableRow icon={faPowerOff} color="downtime"
                          text={t('html.label.averageServerDowntime')}
                          values={[
                              <FormattedTime timeMs={data.avg_server_downtime_30d}/>,
                              <FormattedTime timeMs={data.avg_server_downtime_7d}/>,
                              <FormattedTime timeMs={data.avg_server_downtime_24h}/>
                          ]}/>)}
            <TableRow icon={faPowerOff} color="uptime"
                      text={t('html.label.serverUptime')}
                      values={[
                          <FormattedTime timeMs={data.server_uptime_30d}/>,
                          <FormattedTime timeMs={data.server_uptime_7d}/>,
                          <FormattedTime timeMs={data.server_uptime_24h}/>
                      ]}/>
            {data.avg_server_uptime_30d && (
                <TableRow icon={faPowerOff} color="uptime"
                          text={t('html.label.averageServerUptime')}
                          values={[
                              <FormattedTime timeMs={data.avg_server_uptime_30d}/>,
                              <FormattedTime timeMs={data.avg_server_uptime_7d}/>,
                              <FormattedTime timeMs={data.avg_server_uptime_24h}/>
                          ]}/>)}
            <TableRow icon={faUser} color="players-online" text={t('html.label.averagePlayers')}
                      values={[
                          data.players_30d,
                          data.players_7d,
                          data.players_24h
                      ]}/>
            <TableRow icon={faTachometerAlt} color="tps-average" text={t('html.label.averageTps')}
                      values={[
                          <>{t(data.tps_30d)} {noTPSOnProxies}</>,
                          <>{t(data.tps_7d)} {noTPSOnProxies}</>,
                          <>{t(data.tps_24h)} {noTPSOnProxies}</>
                      ]}/>
            <TableRow icon={faTachometerAlt} color="cpu" text={t('html.label.averageCpuUsage')}
                      values={[
                          data.cpu_30d,
                          data.cpu_7d,
                          data.cpu_24h
                      ]}/>
            <TableRow icon={faMicrochip} color="ram" text={t('html.label.averageRamUsage')}
                      values={[
                          data.ram_30d,
                          data.ram_7d,
                          data.ram_24h
                      ]}/>
            <TableRow icon={faDragon} color="entities" text={t('html.label.averageEntities')}
                      values={[
                          <>{t(data.entities_30d)} {noTPSOnProxies}</>,
                          <>{t(data.entities_7d)} {noTPSOnProxies}</>,
                          <>{t(data.entities_24h)} {noTPSOnProxies}</>
                      ]}/>
            <TableRow icon={faMap} color="chunks"
                      text={<>{t('html.label.averageChunks')}{' '}{data.chunks_30d === 'plugin.generic.unavailable' ?
                          <span title={t('html.description.noSpongeChunks')}><Fa icon={faEye}/></span> : ''}</>}
                      values={[
                          <>{t(data.chunks_30d)} {noTPSOnProxies}</>,
                          <>{t(data.chunks_7d)} {noTPSOnProxies}</>,
                          <>{t(data.chunks_24h)} {noTPSOnProxies}</>
                      ]}/>
            <TableRow icon={faHdd} color="disk"
                      text={t('html.label.maxFreeDisk')}
                      values={[
                          data.max_disk_30d,
                          data.max_disk_7d,
                          data.max_disk_24h
                      ]}/>
            <TableRow icon={faHdd} color="disk"
                      text={t('html.label.minFreeDisk')}
                      values={[
                          data.min_disk_30d,
                          data.min_disk_7d,
                          data.min_disk_24h
                      ]}/>
        </AsNumbersTable>
    )
}

export default PerformanceAsNumbersTable;