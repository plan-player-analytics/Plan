import React, {useEffect, useMemo} from "react";
import {defaultElementOrder, ExtensionTab as DataType} from "../../dataHooks/model/extension/ExtensionData";
import {ExtensionValues} from "./ExtensionValues";
import {ExtensionTables} from "./ExtensionTables";
import {ExtensionLayoutWidths} from "../../dataHooks/model/extension/ExtensionLayoutWidths";
import {optimizeCardOrder} from "../../util/optimizeCardOrder";
import Masonry from "masonry-layout";
import {ExtensionGraphs} from "./ExtensionGraphs";
import {Row} from "react-bootstrap";
import {randomUuid} from "../../util/uuid";

type Props = {
    tab: DataType,
    widths?: ExtensionLayoutWidths
}

export const ExtensionTab = ({tab, widths}: Props) => {
    const uuid = useMemo(randomUuid, []);
    const elementOrder = tab.tabInformation.elementOrder || defaultElementOrder;

    const ordered = widths ? optimizeCardOrder(elementOrder
        .filter(e => widths[e])
        .map(e => ({element: e, widths: {cardWidth: widths[e] || 0}})))
        .map(card => card.element) : elementOrder;

    useEffect(() => {
        const masonryRow = document.getElementById('extension-tab-row');
        if (!masonryRow) return;

        if (!('data' in Masonry)) return;
        if (typeof Masonry.data !== 'function') return;
        let masonry = Masonry.data(masonryRow);
        if (!masonry) {
            masonry = new Masonry(masonryRow, {
                percentPosition: true,
                itemSelector: ".extension-section-wrapper-" + uuid
            });
        }
        return () => {
            if (masonry.element) masonry.destroy();
        }
    }, [tab]);

    return (<Row id="extension-tab-row">
        {ordered.map(type => {
            switch (type) {
                case "GRAPH":
                    return <ExtensionGraphs key={type} uuid={uuid} tab={tab} width={widths?.GRAPH || 12}/>
                case "GROUPS":
                    return null;
                case "STATISTICS":
                    return null;
                case "FLAGS":
                    return null;
                case "VALUES":
                    return <ExtensionValues key={type} uuid={uuid} tab={tab} width={widths?.VALUES || 12}/>
                case "TABLE":
                    return <ExtensionTables key={type} uuid={uuid} tab={tab} width={widths?.TABLE || 12}/>
                default:
                    return null;
            }
        })}
    </Row>);
}