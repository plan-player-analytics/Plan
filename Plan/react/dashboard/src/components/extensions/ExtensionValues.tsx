import React from "react";
import {ExtensionTab} from "../../dataHooks/model/extension/ExtensionData";
import {ExtensionValue} from "./ExtensionValue";
import {CardSection} from "../cards/CardSection";
import {Col} from "react-bootstrap";

type Props = {
    tab: ExtensionTab,
    width: number
}

export const ExtensionValues = ({tab, width}: Props) => {
    if (tab.values.length) {
        return <Col md={width} className="extension-section-wrapper">
            <CardSection>
                {tab.values.map(data => <ExtensionValue key={data.description.name} data={data}/>)}
            </CardSection>
        </Col>
    }

    return null;
};