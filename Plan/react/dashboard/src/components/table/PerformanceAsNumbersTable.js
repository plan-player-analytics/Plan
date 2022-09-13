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
import {faEye} from "@fortawesome/free-regular-svg-icons";
import AsNumbersTable from "./AsNumbersTable";
import {ChartLoader} from "../navigation/Loader";

const PerformanceAsNumbersTable = ({data}) => {
    const {t} = useTranslation();
    if (!data) return <ChartLoader/>;

    return (
        <AsNumbersTable
            headers={[t('html.label.last30days'), t('html.label.last7days'), t('html.label.last24hours')]}
        >
            <TableRow icon={faExclamationCircle} color="red" text={t('html.label.lowTpsSpikes')}
                      values={[
                          data.low_tps_spikes_30d,
                          data.low_tps_spikes_7d,
                          data.low_tps_spikes_24h
                      ]}/>
            <TableRow icon={faPowerOff} color="red"
                      text={t(data.avg_server_downtime_30d ? 'html.label.serverDowntime' : 'html.label.totalServerDowntime') + ' (' + t('generic.noData') + ')'}
                      values={[
                          data.server_downtime_30d,
                          data.server_downtime_7d,
                          data.server_downtime_24h
                      ]}/>
            <TableRow icon={faPowerOff} color="red"
                      text={t('html.label.averageServerDowntime')}
                      values={[
                          data.avg_server_downtime_30d,
                          data.avg_server_downtime_7d,
                          data.avg_server_downtime_24h
                      ]}/>
            <TableRow icon={faUser} color="light-blue" text={t('html.label.averagePlayers')}
                      values={[
                          data.players_30d,
                          data.players_7d,
                          data.players_24h
                      ]}/>
            <TableRow icon={faTachometerAlt} color="orange" text={t('html.label.averageTps')}
                      values={[
                          data.tps_30d,
                          data.tps_7d,
                          data.tps_24h
                      ]}/>
            <TableRow icon={faTachometerAlt} color="amber" text={t('html.label.averageCpuUsage')}
                      values={[
                          data.cpu_30d,
                          data.cpu_7d,
                          data.cpu_24h
                      ]}/>
            <TableRow icon={faMicrochip} color="light-green" text={t('html.label.averageRamUsage')}
                      values={[
                          data.ram_30d,
                          data.ram_7d,
                          data.ram_24h
                      ]}/>
            <TableRow icon={faDragon} color="purple" text={t('html.label.averageEntities')}
                      values={[
                          data.entities_30d,
                          data.entities_7d,
                          data.entities_24h
                      ]}/>
            <TableRow icon={faMap} color="blue-grey"
                      text={<>{t('html.label.averageChunks')}{' '}{data.chunks_30d === 'Unavailable' ?
                          <Fa icon={faEye} title={t('html.description.noSpongeChunks')}/> : ''}</>}
                      values={[
                          data.chunks_30d,
                          data.chunks_7d,
                          data.chunks_24h
                      ]}/>
            <TableRow icon={faHdd} color="green"
                      text={t('html.label.maxFreeDisk')}
                      values={[
                          data.max_disk_30d,
                          data.max_disk_7d,
                          data.max_disk_24h
                      ]}/>
            <TableRow icon={faHdd} color="green"
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