import {useTranslation} from "react-i18next";
import {
    faDragon,
    faExclamationCircle,
    faHdd,
    faMap,
    faMicrochip,
    faPowerOff,
    faStopwatch,
    faTachometerAlt,
    faUser
} from "@fortawesome/free-solid-svg-icons";
import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faQuestionCircle} from "@fortawesome/free-regular-svg-icons";
import {QueryDatapointTable} from "./QueryDatapointTable.tsx";
import {MS_24H, MS_MONTH, MS_WEEK} from "../../util/format/useDateFormatter.js";
import {DatapointType} from "../../dataHooks/model/datapoint/Datapoint.ts";

const PerformanceAsNumbersTable = ({servers}) => {
    const {t} = useTranslation();

    const dataIncludesGameServers = servers && Boolean(servers.filter(server => !server.proxy).length);
    const noTPSOnProxies = !servers || dataIncludesGameServers
        ? ''
        : <span title={t('html.description.performanceNoGameServers')}><Fa icon={faQuestionCircle}/></span>;

    return (
        <QueryDatapointTable filter={{server: servers.map(s => s.serverUUID)}}
                             columns={[{
                                 key: '30d',
                                 filter: {afterMillisAgo: MS_MONTH}
                             }, {
                                 key: '7d',
                                 filter: {afterMillisAgo: MS_WEEK}
                             }, {
                                 key: '1d',
                                 filter: {afterMillisAgo: MS_24H}
                             }]}
                             rows={[{
                                 dataType: DatapointType.TPS_LOW_SPIKES,
                                 color: "tps-low-spikes",
                                 icon: faExclamationCircle,
                                 text: t('html.label.lowTpsSpikes')
                             }, { // TODO Total and average/server downtime and uptime
                                 dataType: DatapointType.DOWNTIME,
                                 color: "downtime",
                                 icon: faPowerOff,
                                 text: t('html.label.serverDowntime') + ' (' + t('generic.noData') + ')'
                             }, {
                                 dataType: DatapointType.UPTIME,
                                 color: "uptime",
                                 icon: faPowerOff,
                                 text: t('html.label.serverUptime')
                             }, {
                                 dataType: DatapointType.PLAYERS_ONLINE_AVERAGE,
                                 color: "players-online",
                                 icon: faUser,
                                 text: t('html.label.averagePlayers')
                             }, {
                                 dataType: DatapointType.TPS_AVERAGE,
                                 color: "tps-average",
                                 icon: faTachometerAlt,
                                 text: <>{t('html.label.averageTps')} {noTPSOnProxies}</>,
                                 key: "average-tps"
                             }, {
                                 dataType: DatapointType.MSPT_AVERAGE,
                                 color: "mspt-average",
                                 icon: faStopwatch,
                                 text: <>{t('html.label.msptAverage')} {noTPSOnProxies}</>,
                                 key: "average-mspt"
                             }, {
                                 dataType: DatapointType.CPU_AVERAGE,
                                 color: "cpu",
                                 icon: faTachometerAlt,
                                 text: t('html.label.averageCpuUsage')
                             }, {
                                 dataType: DatapointType.RAM_AVERAGE,
                                 color: "ram",
                                 icon: faMicrochip,
                                 text: t('html.label.averageRamUsage')
                             }, {
                                 dataType: DatapointType.ENTITIES_AVERAGE,
                                 color: "entities",
                                 icon: faDragon,
                                 text: <>{t('html.label.averageEntities')} {noTPSOnProxies}</>,
                                 key: "average-entities"
                             }, {
                                 dataType: DatapointType.CHUNKS_AVERAGE,
                                 color: "chunks",
                                 icon: faMap,
                                 text: <>{t('html.label.averageChunks')} {noTPSOnProxies}</>,
                                 key: "average-chunks"
                             }, {
                                 dataType: DatapointType.DISK_MAX,
                                 color: "disk",
                                 icon: faHdd,
                                 text: t('html.label.maxFreeDisk'),
                                 hidden: servers.length > 1,
                             }, {
                                 dataType: DatapointType.DISK_MIN,
                                 color: "disk",
                                 icon: faHdd,
                                 text: t('html.label.minFreeDisk'),
                                 hidden: servers.length > 1,
                             }]}/>
    )
}

export default PerformanceAsNumbersTable;