import {ExtensionStatisticsData} from "../../dataHooks/model/extension/ExtensionData";
import {StatisticsTable} from "../table/StatisticsTable";
import ExtensionIcon from "./ExtensionIcon";

type Props = {
    statistics: ExtensionStatisticsData;
}

export const ExtensionStatisticsTable = ({statistics}: Props) => {
    return (
        <StatisticsTable statistics={[{serverUUID: '', statistics: statistics.statistics.valueByKey}]}
                         tableColor={statistics.tableColorClass}
                         icon={<ExtensionIcon icon={{...statistics.icon, colorClass: ''}}/>}
                         selectedOptions={[0]}
                         serversWithData={[{serverUUID: '', serverName: '', proxy: false}]}
                         serverUUIDs={['']}
                         splitter={':'}/>
    )
}