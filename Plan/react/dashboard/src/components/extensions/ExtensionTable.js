import React, {useCallback, useState} from 'react';
import {useTheme} from "../../hooks/themeHook";
import {useTranslation} from "react-i18next";
import ExtensionIcon, {toExtensionIconHtmlString} from "./ExtensionIcon";
import DataTablesTable from "../table/DataTablesTable";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faSort, faSortDown, faSortUp} from "@fortawesome/free-solid-svg-icons";

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
        pagingType: "numbers",
        order: [[1, "desc"]]
    }
    return (
        <DataTablesTable id={id} options={options}/>
    )
}

const sortComparator = (columnIndex) => (rowA, rowB) => {
    const a = rowA[columnIndex].valueUnformatted;
    const b = rowB[columnIndex].valueUnformatted;
    if (a === b) return 0;
    if (isNaN(Number(a)) || isNaN(Number(b))) {
        return String(a).toLowerCase().localeCompare(String(b).toLowerCase());
    } else {
        const numA = Number(a);
        const numB = Number(b);
        if (numA < numB) return -1;
        if (numA > numB) return 1;
        return 0;
    }
}

const sortRows = (rows, sortIndex, sortReversed) => {
    if (sortIndex === undefined) return rows;

    const comparator = sortComparator(sortIndex);
    const sorted = rows.sort(comparator);
    if (sortReversed) return rows.reverse();
    return sorted;
}

const ExtensionColoredTable = ({table}) => {
    const {nightModeEnabled} = useTheme();
    const {t} = useTranslation();

    const [sortBy, setSortBy] = useState(undefined);
    const [sortReverse, setSortReverse] = useState(false);
    const changeSort = useCallback(index => {
        if (index === sortBy) {
            setSortReverse(!sortReverse);
        } else {
            setSortBy(index);
            setSortReverse(false);
        }

    }, [sortBy, setSortBy, sortReverse, setSortReverse]);

    const rows = table.table.rows.length ? sortRows(table.table.rows, sortBy, sortReverse)
            .map((row, i) => <tr key={JSON.stringify(row)}>{row.map((value, j) => <td
                key={JSON.stringify(value)}>{value.value || String(value)}</td>)}</tr>) :
        <tr>{table.table.columns.map((column, i) =>
            <td key={JSON.stringify(column)}>{i === 0 ? t('generic.noData') : '-'}</td>)}
        </tr>

    return (
        <table className={"table table-striped" + (nightModeEnabled ? " table-dark" : '')}>
            <thead className={table.tableColorClass}>
            <tr>
                {table.table.columns.map((column, i) => <th className={'extension-table-header'}
                                                            key={JSON.stringify(column)} onClick={() => changeSort(i)}>
                    <ExtensionIcon icon={table.table.icons[i]}/>
                    &nbsp;
                    {column}
                    &nbsp;
                    <FontAwesomeIcon className={sortBy === i ? '' : 'opacity-50'}
                                     icon={sortBy === i ? (sortReverse ? faSortDown : faSortUp) : faSort}/>
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