import React, {useEffect, useState} from 'react';
import ErrorView from "../ErrorView.tsx";
import LoadIn from "../../components/animation/LoadIn.tsx";
import {Card, Col, Row} from "react-bootstrap";
import ExtensionCard from "../../components/extensions/ExtensionCard";
import {useParams} from "react-router";
import {useTranslation} from "react-i18next";
import Loader from "../../components/navigation/Loader";
import {useServerExtensionContext} from "../../hooks/serverExtensionDataContext";

const PluginData = ({plugin}) => {
    const {t} = useTranslation();
    const {extensionData, extensionDataLoadingError} = useServerExtensionContext();
    const [extension, setExtension] = useState(undefined);

    useEffect(() => {
        setExtension(extensionData?.extensions?.find(extension => extension.extensionInformation.pluginName === plugin))
    }, [setExtension, extensionData, plugin])

    if (extensionDataLoadingError) return <ErrorView error={extensionDataLoadingError}/>;

    if (!extension) {
        return (
            <LoadIn>
                <section className="server_plugin_data">
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
                <Row id={"extension-" + extension.extensionInformation.pluginName}
                     style={{overflowY: 'hidden'}}>
                    <Col md={12}>
                        <ExtensionCard extension={extension}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

const ServerWidePluginData = () => {
    const {plugin} = useParams();
    const [previousPlugin, setPreviousPlugin] = useState(undefined);

    // Prevents React from reusing the extension component of two different plugins, leading to DataTables errors.
    useEffect(() => {
        setPreviousPlugin(plugin);
    }, [plugin, setPreviousPlugin]);

    if (plugin !== previousPlugin) {
        return <></>
    }

    return (<PluginData plugin={plugin}/>)
};

export default ServerWidePluginData