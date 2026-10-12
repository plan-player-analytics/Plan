import {ExtensionTab} from "../../dataHooks/model/extension/ExtensionData";
import {Col} from "react-bootstrap";
import {CardSection} from "../cards/CardSection";
import React from "react";
import {ExtensionStatisticsTable} from "./ExtensionStatisticsTable";

type Props = {
    uuid: string;
    tab: ExtensionTab;
    width: number;
}

export const ExtensionStatistics = ({uuid, tab, width}: Props) => {
    return (<>
        {tab.statisticsData.map(statistics => (
            <Col key={statistics.providerName} md={width} className={"extension-section-wrapper-" + uuid}>
                <CardSection noPadding>
                    <ExtensionStatisticsTable statistics={statistics}/>
                </CardSection>
            </Col>
        ))}
    </>);
}