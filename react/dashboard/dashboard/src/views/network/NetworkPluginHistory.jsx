import {Col, InputGroup} from "react-bootstrap";
import React, {useEffect, useState} from "react";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import PluginHistoryCard from "../../components/cards/common/PluginHistoryCard";
import {useDataRequest} from "../../hooks/dataFetchHook";
import {fetchPluginHistory} from "../../service/serverService";
import PluginCurrentCard from "../../components/cards/common/PluginCurrentCard";
import {useMetadata} from "../../hooks/metadataHook";
import Select from "../../components/input/Select";
import {useTranslation} from "react-i18next";
import InputGroupText from "react-bootstrap/InputGroupText";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faServer} from "@fortawesome/free-solid-svg-icons";

const NetworkPluginHistory = () => {
    const {t} = useTranslation();
    const {authRequired, hasPermission} = useAuth();

    const seeHistory = authRequired && hasPermission('page.network.plugin.history');

    const {networkMetadata} = useMetadata();
    const [serverOptions, setServerOptions] = useState([]);
    const [selectedOption, setSelectedOption] = useState(0);
    const [identifier, setIdentifier] = useState(undefined);
    useEffect(() => {
        if (networkMetadata) {
            const options = networkMetadata.servers;
            setServerOptions(options);

            const indexOfProxy = options
                .findIndex(option => option.serverName === networkMetadata.currentServer.serverName);

            setSelectedOption(indexOfProxy);
        }
    }, [networkMetadata, setSelectedOption, setServerOptions]);
    useEffect(() => {
        if (serverOptions.length) {
            setIdentifier(serverOptions[selectedOption].serverUUID);
        }
    }, [selectedOption, serverOptions])

    let {data, loadingError} = useDataRequest(fetchPluginHistory, [identifier], Boolean(identifier) && seeHistory);
    if (!identifier) data = {history: []};
    return (
        <LoadIn>
            {seeHistory && <section id="network-plugin-history">
                <ExtendableRow id={'row-network-plugin-history-0'}>
                    <Col md={4} className={"mb-4"}>
                        <InputGroup>
                            <InputGroupText><FontAwesomeIcon icon={faServer}/> {t('html.label.serverSelector')}
                            </InputGroupText>
                            <Select options={serverOptions.map(server => server.serverName)}
                                    selectedIndex={selectedOption} setSelectedIndex={setSelectedOption}/>
                        </InputGroup>
                    </Col>
                </ExtendableRow>
                <ExtendableRow id={'row-network-plugin-history-1'}>
                    <Col md={6}>
                        <PluginCurrentCard data={data} loadingError={loadingError}/>
                    </Col>
                    <Col md={6}>
                        <PluginHistoryCard data={data} loadingError={loadingError}/>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
}

export default NetworkPluginHistory;