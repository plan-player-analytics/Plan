import React, {useEffect} from 'react';
import {useServer} from "../layout/ServerPage";
import Masonry from "masonry-layout";
import LoadIn from "../../components/animation/LoadIn";
import {Card, Col, Row} from "react-bootstrap-v5";
import ExtensionCard, {ExtensionCardWrapper} from "../../components/extensions/ExtensionCard";

const ServerPluginData = () => {
    const extensionData = useServer();
    console.log(extensionData);
    const extensions = extensionData ? extensionData.extensions.filter(extension => !extension.wide) : [];

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
    }, [])

    if (!extensions || !extensions.length) {
        return (
            <LoadIn>
                <section className="server_plugin_data">
                    <Row style={{overflowY: 'hidden'}}>
                        <Col md={12}>
                            <Card>
                                <Card.Body>
                                    <p>No Extension data</p>
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
            <section className="server_plugin_data">
                <Row id="extension-masonry-row"
                     data-masonry='{"percentPosition": true, "itemSelector": ".extension-wrapper"}'
                     style={{overflowY: 'hidden'}}>
                    {extensions.map((extension, i) =>
                        <ExtensionCardWrapper key={'ext-' + i} extension={extension}>
                            <ExtensionCard extension={extension}/>
                        </ExtensionCardWrapper>
                    )}
                </Row>
            </section>
        </LoadIn>
    )
};

export default ServerPluginData