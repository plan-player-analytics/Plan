import {ExtensionData} from "../../dataHooks/model/extension/ExtensionData";
import {Card, Col} from "react-bootstrap";
import React, {useState} from "react";
import Masonry from "masonry-layout";
import ExtensionIcon from "./ExtensionIcon";
import {TopNavTabs} from "../layout/TopNavTabs";
import {ExtensionTab} from "./ExtensionTab";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faChevronDown, faChevronUp} from "@fortawesome/free-solid-svg-icons";

type Props = {
    extension: ExtensionData;
}

export const ExtensionCard = ({extension}: Props) => {
    const [collapsed, setCollapsed] = useState(false);

    const reorder = () => {
        requestAnimationFrame(() => {
            const masonryRow = document.getElementById('extension-masonry-row');
            if (!('data' in Masonry)) return;
            if (typeof Masonry.data !== 'function') return;
            const masonry = Masonry.data(masonryRow);
            if (masonry) {
                masonry.layout();
            }
        });
    }
    const onCollapse = () => {
        setCollapsed(!collapsed);
        reorder();
    }

    return (
        <Col md={extension.widths?.cardWidth || 12} className="extension-wrapper">
            <Card>
                <Card.Header>
                    <h6 className="col-text" style={{width: "100%"}}>
                        <ExtensionIcon
                            icon={extension.extensionInformation.icon}/> {extension.extensionInformation.pluginName}
                        <button style={{margin: "-0.5rem", marginLeft: 0}}
                                className={"btn col-text float-end " + (collapsed ? "collapsed" : "")}
                                onClick={onCollapse}>
                            <FontAwesomeIcon icon={collapsed ? faChevronDown : faChevronUp}/>
                        </button>
                        {/*<TextWithProgressBar positiveCount={0} total={0}*/}
                        {/*                     titleKey={'html.label.titlePlayersHaveData'}*/}
                        {/*                     textKey={'html.label.playersWithData'}/>*/}
                    </h6>
                </Card.Header>
                <div className={"collapse " + (collapsed ? '' : "show")}>
                    <TopNavTabs onChange={reorder}
                                tabs={extension.onlyGenericTab ? [] : extension.tabs.map(tab => ({
                                    key: tab.tabInformation.tabName,
                                    header: <><ExtensionIcon
                                        icon={tab.tabInformation.icon}/> {tab.tabInformation.tabName}</>
                                }))}>
                        {activeIndex => <ExtensionTab tab={extension.tabs[activeIndex]} widths={extension.widths}/>}
                    </TopNavTabs>
                </div>
            </Card>
        </Col>
    )
}