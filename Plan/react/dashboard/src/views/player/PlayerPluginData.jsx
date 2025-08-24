import React, {useEffect} from "react";
import ExtensionCard, {ExtensionCardWrapper} from "../../components/extensions/ExtensionCard";
import {Card, Col, Row} from "react-bootstrap";
import {useParams} from "react-router";
import Masonry from "masonry-layout";
import {usePlayer} from "../layout/PlayerPage";
import LoadIn from "../../components/animation/LoadIn";
import {useAuth} from "../../hooks/authenticationHook";

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

    return (
        <LoadIn>
            <section className="player_plugin_data" id={"player-plugin-data"}>
                <Row id="extension-masonry-row"
                     data-masonry='{"percentPosition": true, "itemSelector": ".extension-wrapper"}'
                     style={{overflowY: 'hidden'}}>
                    {extensions?.extensionData?.map((extension, i) =>
                        <ExtensionCardWrapper key={'ext-' + i} extension={extension}>
                            <ExtensionCard extension={extension}/>
                        </ExtensionCardWrapper>
                    )}
                </Row>
            </section>
        </LoadIn>
    )
}

export default PlayerPluginData;