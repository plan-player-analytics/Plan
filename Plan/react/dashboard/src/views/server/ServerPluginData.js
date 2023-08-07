import React, {useEffect, useMemo} from 'react';
import Masonry from "masonry-layout";
import LoadIn from "../../components/animation/LoadIn";
import {Card, Col, Row} from "react-bootstrap";
import ExtensionCard, {ExtensionCardWrapper} from "../../components/extensions/ExtensionCard";
import Loader from "../../components/navigation/Loader";
import {useTranslation} from "react-i18next";
import {useServerExtensionContext} from "../../hooks/serverExtensionDataContext";
import ErrorView from "../ErrorView";
import {useAuth} from "../../hooks/authenticationHook";

const ServerPluginData = () => {
    const {hasPermission} = useAuth();
    const {t} = useTranslation();
    const {extensionData, extensionDataLoadingError, proxy} = useServerExtensionContext();
    const extensions = useMemo(() => extensionData?.extensions ? extensionData.extensions.filter(extension => !extension.wide) : [], [extensionData]);
    const seePlugins = hasPermission(proxy ? 'page.network.plugins' : 'page.server.plugins');

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
    }, [extensions]);

    if (extensionDataLoadingError) return <ErrorView error={extensionDataLoadingError}/>;

    if (!seePlugins) {
        return <></>
    }

    if (!extensions?.length) {
        return (
            <LoadIn>
                <section className="server_plugin_data" id={"server-plugin-data"}>
                    <Row style={{overflowY: 'hidden'}}>
                        <Col md={12}>
                            <Card>
                                <Card.Body>
                                    <p>{extensionData ? t('html.text.noExtensionData') : <Loader/>}</p>
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