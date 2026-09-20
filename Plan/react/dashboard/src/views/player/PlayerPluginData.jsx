import React, {useEffect} from "react";
import {Card, Col, Row} from "react-bootstrap";
import {useParams} from "react-router";
import Masonry from "masonry-layout";
import {usePlayer} from "../layout/PlayerPage";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {ExtensionCard} from "../../components/extensions/ExtensionCard.tsx";
import {computeLayoutWidths} from "../../dataHooks/model/extension/ExtensionLayoutWidths.ts";
import {optimizeCardOrder} from "../../util/optimizeCardOrder.ts";

const PlayerPluginData = () => {
    const {hasPermission} = useAuth();
    const {player} = usePlayer();
    const {serverName} = useParams();

    const extensions = player.extensions ? player.extensions.find(extension => extension.serverName === serverName) : {};

    useEffect(() => {
        const masonryRow = document.getElementById('extension-masonry-row');
        if (!masonryRow) return;
        let masonry = Masonry.data(masonryRow);
        if (!masonry) {
            masonry = new Masonry(masonryRow, {"percentPosition": true, "itemSelector": ".extension-wrapper"});
        }
        return () => {
            if (masonry.element) masonry.destroy();
        }
    }, [serverName])

    if (!hasPermission('page.player.plugins')) {
        return <></>;
    }

    if (!extensions?.extensionData?.length) {
        return (
            <LoadIn>
                <section className="player_plugin_data" id={"player-plugin-data"}>
                    <Row style={{overflowY: 'hidden'}}>
                        <Col md={12}>
                            <Card>
                                <Card.Body>
                                    <p>No Extension data for {serverName}</p>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>
                </section>
            </LoadIn>
        )
    }

    extensions?.extensionData?.forEach(extension => extension.widths = computeLayoutWidths(extension));
    const ordered = optimizeCardOrder(extensions?.extensionData);

    return (
        <LoadIn>
            <section className="player_plugin_data" id={"player-plugin-data"}>
                <Row id="extension-masonry-row"
                     data-masonry='{"percentPosition": true, "itemSelector": ".extension-wrapper"}'
                     style={{overflowY: 'hidden'}}>
                    {ordered.map(extension =>
                        <ExtensionCard key={extension.extensionInformation.pluginName} extension={extension}/>
                    )}
                </Row>
            </section>
        </LoadIn>
    )
}

export default PlayerPluginData;