import React, {ReactNode, useState} from 'react';
import ExtensionIcon from "./ExtensionIcon";
import DataTablesTable from "../table/DataTablesTable";
import ColoredText from "../text/ColoredText";
import {Link} from "react-router";
import FormattedTime from "../text/FormattedTime.jsx";
import FormattedDate from "../text/FormattedDate";
import {ExtensionTableCell, ExtensionTableData} from "../../dataHooks/model/extension/ExtensionData";
import {useTranslation} from "react-i18next";
import styles from './ExtensionTable.module.scss';

type Props = { table: ExtensionTableData }

const ExtensionDataTable = ({table}: Props) => {
    const {t} = useTranslation();

    const [id] = useState("extension-table-" + Date.now() + "-" + (Math.floor(Math.random() * 100000)));
    const mapToCell = (cell: ExtensionTableCell) => {
        const value = cell.value;
        switch (cell.format) {
            case 'BOOLEAN':
                return t(value ? 'plugin.generic.yes' : 'plugin.generic.no')
            case 'TIME_MILLISECONDS':
                return <FormattedTime timeMs={value}/>;
            case 'DATE_YEAR':
                return <FormattedDate date={value}/>;
            case 'DATE_SECOND':
                return <FormattedDate date={value} includeSeconds/>;
            case 'PLAYER_NAME':
                return <Link to={'/player/' + value}>{value}</Link>;
            case 'CHAT_COLORED':
            default:
                return <ColoredText text={value || String(value)}/>
        }
    };

    const data = {
        columns: table.table.columns.map((column, i) => {
            return {
                title: <><ExtensionIcon icon={table.table.icons[i]}/> {column}</>,
                data: {
                    "_": `col${i}Value`,
                    display: `col${i}Display`
                },
            };
        }),
        data: table.table.rows.map((row) => {
            const dataRow: { [key: string]: string | ReactNode } = {};
            row.forEach((cell, j) => {
                dataRow[`col${j}Value`] = cell.value || cell;
                dataRow[`col${j}Display`] = mapToCell(cell);
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
    const rowKeyFunction = (row: any, column: any) => {
        const valueFields = Object.entries(row)
            .filter(e => typeof e[0] === 'string' && e[0].includes('Value'))
            .map(e => String(e[1]));
        return valueFields.join('-') + '-' + String(column?.data?._);
    }

    return (
        <DataTablesTable
            id={id}
            options={options}
            rowKeyFunction={rowKeyFunction}
            colorClass={table.tableColorClass}
            className={styles.extensionTable}
            expandComponent={undefined}
            clickableRows={undefined}
        />
    )
}

const ExtensionTable = ({table}: Props) => {
    return <ExtensionDataTable table={table}/>;
}

export default ExtensionTable