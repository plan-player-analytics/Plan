import {PlanResponse} from "../../../service/PlanResponse";
import {IconPrefix} from "@fortawesome/free-brands-svg-icons";
import {IconName} from "@fortawesome/fontawesome-svg-core";
import {ExtensionLayoutWidths} from "./ExtensionLayoutWidths";
import {IconFamily} from "../../../util/icons";

export type ExtensionIcon = {
    family: IconFamily;
    familyClass: IconPrefix;
    color: string;
    colorClass: string;
    iconName: IconName;
}

export type ElementOrder = 'VALUES' | 'GRAPH' | 'TABLE' | 'GROUPS' | 'STATISTICS' | 'FLAGS';
export const defaultElementOrder: ElementOrder[] = ['VALUES', 'GRAPH', 'TABLE', 'GROUPS', 'STATISTICS', 'FLAGS'];

export type ExtensionType = 'BOOLEAN' | 'DOUBLE' | 'PERCENTAGE' | 'NUMBER' | 'LINK' | 'STRING' | 'COMPONENT'
    | 'TIME_MILLISECONDS'
    | 'DATE_YEAR'
    | 'DATE_SECOND';

export type TableColumnFormat =
    'BOOLEAN'
    | 'PLAYER_NAME'
    | 'CHAT_COLORED'
    | 'TIME_MILLISECONDS'
    | 'DATE_YEAR'
    | 'DATE_SECOND'
    | 'NONE';

export type ExtensionInformation = {
    pluginName: string;
    icon: ExtensionIcon;
}

export type ExtensionDescription = {
    name: string;
    text: string;
    description?: string;
    icon: ExtensionIcon;
    priority: number;
}

export type ExtensionValue = {
    description: ExtensionDescription;
    type: ExtensionType;
    value: any;
}

export type ExtensionTableData = {
    tableName: string;
    tableColor: string;
    tableColorClass: string;
    wide: boolean;
    table: ExtensionTable;
}

export type ExtensionTableCell = { value: any; format: TableColumnFormat }

export type ExtensionTable = {
    columns: string[];
    icons: ExtensionIcon[];
    rows: ExtensionTableCell[][]
}

export type ExtensionTab = {
    tabInformation: ExtensionTabInformation;
    values: ExtensionValue[];
    tableData: ExtensionTableData[];
    graphNames: string[];
}

export type ExtensionTabInformation = {
    tabName: string;
    icon?: ExtensionIcon;
    elementOrder?: ElementOrder[];
    tabPriority: number;
}

export type ExtensionData = {
    extensionInformation: ExtensionInformation;
    tabs: ExtensionTab[];
    onlyGenericTab: boolean;
    wide: boolean;
    widths?: ExtensionLayoutWidths;
}

export type ExtensionDataResponseContent = {
    extensions: ExtensionData[];
}

export type ExtensionDataResponse = PlanResponse<ExtensionDataResponseContent>;