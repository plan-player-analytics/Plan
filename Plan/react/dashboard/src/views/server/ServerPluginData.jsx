import React, {useEffect, useMemo} from 'react';
import Masonry from "masonry-layout";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {Card, Col, Row} from "react-bootstrap";
import Loader from "../../components/navigation/Loader.tsx";
import {useTranslation} from "react-i18next";
import {useServerExtensionContext} from "../../hooks/serverExtensionDataContext";
import ErrorView from "../ErrorView.tsx";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {ExtensionCard} from "../../components/extensions/ExtensionCard.tsx";
import {computeLayoutWidths} from "../../dataHooks/model/extension/ExtensionLayoutWidths.ts";
import {optimizeCardOrder} from "../../util/optimizeCardOrder.ts";

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
            masonry = new Masonry(masonryRow, {
                percentPosition: true,
                itemSelector: ".extension-wrapper"
            });
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
                    <Row>
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

    // TODO compute based on window size
    //      windowWidth < 1000 ? 6 : 4;
    extensions.forEach(extension => extension.widths = computeLayoutWidths(extension));
    const ordered = optimizeCardOrder(extensions);

    return (
        <LoadIn>
            <section className="server_plugin_data">
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
};

export default ServerPluginData