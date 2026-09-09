import React from "react";
import {GenericFilter} from "../../dataHooks/model/GenericFilter";
import {DatapointType, NumericDatapointType} from "../../dataHooks/model/datapoint/Datapoint";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import ComparingLabel from "../trend/ComparingLabel";
import {useTheme} from "../../hooks/themeHook";
import {IconProp} from "@fortawesome/fontawesome-svg-core";
import {calculatePermission, QueryDatapointTrend, QueryDatapointValue} from "../datapoint/QueryDatapoint";
import FormattedDay from "../text/FormattedDay";
import {useAuth} from "../../hooks/authenticationHook";
import {useTranslation} from "react-i18next";
import {MS_24H, MS_MONTH, MS_WEEK} from "../../util/format/useDateFormatter";

type Column = {
    filter: GenericFilter;
    label?: string;
    key: string;
}

type Row = {
    dataType: DatapointType;
    permission?: string;
    icon: IconProp;
    key?: string;
    text: string | React.ReactNode;
    color: string;
    filter?: GenericFilter;
    bold?: boolean;
    title?: string;
    hidden?: boolean;
    indent?: boolean;
    boldBottom?: boolean;
}

type Props = {
    comparisonHeader?: string;
    filter: GenericFilter;
    columns: Column[];
    rows: Row[];
    showTrend?: boolean;
}

const FilterHeader = ({filter, label}: Column) => {
    const {t} = useTranslation();
    if (label) {
        return <th>{label}</th>;
    }
    if (filter.afterMillisAgo) {
        if (filter.beforeMillisAgo === undefined) {
            if (filter.afterMillisAgo === MS_MONTH) return <th>{t('html.label.last30days')}</th>;
            if (filter.afterMillisAgo === MS_WEEK) return <th>{t('html.label.last7days')}</th>;
            if (filter.afterMillisAgo === MS_24H) return <th>{t('html.label.last24hours')}</th>;
        }

        return <th>
            <FormattedDay date={Date.now() - filter.afterMillisAgo}/> - <FormattedDay
            date={Date.now() - (filter.beforeMillisAgo || 0)}/>
        </th>
    }
    if (filter.after) {
        return <th><FormattedDay date={filter.after}/> - <FormattedDay date={filter.before || Date.now()}/></th>
    }
    return null;
}

export const QueryDatapointTable = ({comparisonHeader, filter, columns, rows, showTrend}: Props) => {
    const {t} = useTranslation();
    const {nightModeEnabled} = useTheme();
    const {hasPermission} = useAuth();

    return <table className={"table table-striped" + (nightModeEnabled ? " table-dark" : '')}>
        <thead className={"sticky-top"}>
        <tr>
            <th>
                {comparisonHeader && <ComparingLabel>{comparisonHeader}</ComparingLabel>}
            </th>
            {columns.map(column => <FilterHeader key={column.key} filter={column.filter} label={column.label}/>)}
            {showTrend && <th>{t('html.label.trend')}</th>}
        </tr>
        </thead>
        <tbody>
        {rows.filter(row => !row.hidden)
            .filter(row => hasPermission(calculatePermission(row.dataType, filter)))
            .map(row => <tr key={row.key || String(row.text)} title={row.title}
                            style={row.boldBottom ? {borderBottomWidth: "3px"} : undefined}>
                <td><Fa icon={row.icon} className={'col-' + row.color}
                        style={row.indent ? {marginLeft: "1rem"} : undefined}/> {row.text}</td>
                {columns.map(column => <td key={column.key + row.text}>
                    <QueryDatapointValue dataType={row.dataType}
                                         filter={{...filter, ...column.filter, ...row.filter}}
                                         noDataFallback={"-"}/>
                </td>)}
                {showTrend && <td>
                    <QueryDatapointTrend filter={{...filter, ...columns[0].filter}}
                                         filter2={{...filter, ...columns[1].filter}}
                                         dataType={row.dataType as NumericDatapointType}/>
                </td>}
            </tr>)}
        </tbody>
    </table>
}