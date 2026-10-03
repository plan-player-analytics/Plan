import ExtensionTable from "./ExtensionTable";
import React from "react";
import {ExtensionTab} from "../../dataHooks/model/extension/ExtensionData";
import {Col} from "react-bootstrap";
import {CardSection} from "../cards/CardSection";

type Props = {
    uuid: string;
    tab: ExtensionTab;
    width: number;
}

export const ExtensionTables = ({uuid, tab, width}: Props) => {
    return (<>
        {tab.tableData.map(table => (
            <Col key={table.tableName} md={width} className={"extension-section-wrapper-" + uuid}>
                <CardSection noPadding>
                    <ExtensionTable table={table}/>
                </CardSection>
            </Col>
        ))}
    </>);
}