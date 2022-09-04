import React, {useState} from 'react';
import {useTheme} from "../../hooks/themeHook";
import {useTranslation} from "react-i18next";
import ExtensionIcon, {toExtensionIconHtmlString} from "./ExtensionIcon";
import DataTablesTable from "../table/DataTablesTable";

const ExtensionDataTable = ({table}) => {
    const [id] = useState("extension-table-" + new Date().getTime() + "-" + (Math.floor(Math.random() * 100000)));

    const data = {
        columns: table.table.columns.map((column, i) => {
            return {
                title: toExtensionIconHtmlString(table.table.icons[i]) + ' ' + column,
                data: {
                    "_": `col${i}.v`,
                    display: `col${i}.d`
                },
            };
        }),
        data: table.table.rows.map((row) => {
            const dataRow = {};
            row.forEach((cell, j) => dataRow[`col${j}`] = {
                v: cell['valueUnformatted'] || cell.value || cell,
                d: cell.value || cell
            });
            return dataRow;
        })
    };
    const options = {
        responsive: true,
        deferRender: true,
        columns: data.columns,
        data: data.data,
        order: [[1, "desc"]]
    }
    return (
        <DataTablesTable id={id} options={options}/>
    )
}

const ExtensionColoredTable = ({table}) => {
    const {nightModeEnabled} = useTheme();
    const {t} = useTranslation();

    const rows = table.table.rows.length ? table.table.rows.map((row, i) => <tr key={i}>{row.map((value, j) => <td
            key={i + '' + j}>{value.value || String(value)}</td>)}</tr>) :
        <tr>{table.table.columns.map((column, i) =>
            <td key={i}>{i === 0 ? t('generic.noData') : '-'}</td>)}
        </tr>

    return (
        <table className={"table table-striped" + (nightModeEnabled ? " table-dark" : '')}>
            <thead className={table.tableColorClass}>
            <tr>
                {table.table.columns.map((column, i) => <th key={i}><ExtensionIcon
                    icon={table.table.icons[i]}/> {column}
                </th>)}
            </tr>
            </thead>
            <tbody>
            {rows}
            </tbody>
        </table>
    )
}

const ExtensionTable = ({table}) => {
    const tableLength = table.table.rows.length;

    if (tableLength > 10) {
        return <ExtensionDataTable table={table}/>
    } else {
        return <ExtensionColoredTable table={table}/>
    }
}

export default ExtensionTable