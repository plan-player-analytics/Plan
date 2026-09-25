import ExtensionTable from "./ExtensionTable";
import React from "react";
import {ExtensionTab} from "../../dataHooks/model/extension/ExtensionData";
import {Col} from "react-bootstrap";

type Props = {
    tab: ExtensionTab;
    width: number;
}

export const ExtensionTables = ({tab, width}: Props) => {
    return (<>
        {tab.tableData.map(table => (
            <Col key={table.tableName} md={width} className="extension-section-wrapper">
                <ExtensionTable table={table}/>
            </Col>
        ))}
    </>);
}