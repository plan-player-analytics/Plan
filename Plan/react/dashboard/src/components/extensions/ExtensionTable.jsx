import React, {useState} from 'react';
import ExtensionIcon from "./ExtensionIcon";
import DataTablesTable from "../table/DataTablesTable";
import ColoredText from "../text/ColoredText";
import {baseAddress} from "../../service/backendConfiguration";

const ExtensionDataTable = ({table}) => {
    const [id] = useState("extension-table-" + new Date().getTime() + "-" + (Math.floor(Math.random() * 100000)));

    const mapToCell = (value, j) => {
        if (String(value).startsWith("<a class=\"link\" href=\"")) {
            let linkHtml = value || String(value);
            linkHtml = linkHtml.replace("../", baseAddress + '/');
            return <span dangerouslySetInnerHTML={{__html: linkHtml}}/>;
        }
        return <ColoredText text={value || String(value)}/>
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
            const dataRow = {};
            row.forEach((cell, j) => {
                dataRow[`col${j}Value`] = cell['valueUnformatted'] || cell.value || cell;
                dataRow[`col${j}Display`] = mapToCell(cell.value || cell, j);
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
    const rowKeyFunction = (row, column) => {
        // Generate a unique key by joining all value fields as strings
        const valueFields = Object.entries(row)
            .filter(e => typeof e[0] === 'string' && e[0].includes('Value'))
            .map(e => String(e[1]));
        return valueFields.join('-') + '-' + String(column?.data?._);
    }

    return (
        <DataTablesTable id={id} options={options} rowKeyFunction={rowKeyFunction} colorClass={table.tableColorClass}/>
    )
}

const ExtensionTable = ({table}) => {
    return <ExtensionDataTable table={table}/>;
}

export default ExtensionTable