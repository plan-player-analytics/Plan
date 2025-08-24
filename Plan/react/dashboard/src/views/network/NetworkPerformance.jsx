import React, {useCallback, useEffect, useState} from 'react';
import LoadIn from "../../components/animation/LoadIn";
import {Card, Col} from "react-bootstrap";
import {useMetadata} from "../../hooks/metadataHook";
import CardHeader from "../../components/cards/CardHeader";
import {faServer} from "@fortawesome/free-solid-svg-icons";
import MultiSelect from "../../components/input/MultiSelect";
import {useTranslation} from "react-i18next";
import {fetchOptimizedPerformance} from "../../service/serverService";
import {fetchNetworkPerformanceOverview} from "../../service/networkService";
import PerformanceAsNumbersCard from "../../components/cards/server/tables/PerformanceAsNumbersCard";
import {useNavigation} from "../../hooks/navigationHook";
import {mapPerformanceDataToSeries} from "../../util/graphs";
import PerformanceGraphsCard from "../../components/cards/network/PerformanceGraphsCard";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";
import ActionButton from "../../components/input/button/ActionButton.jsx";

const NetworkPerformance = () => {
    const {hasPermission} = useAuth();
    const {t} = useTranslation();
    const {networkMetadata} = useMetadata();
    const {updateRequested} = useNavigation();

    const [serverOptions, setServerOptions] = useState([]);
    const [selectedOptions, setSelectedOptions] = useState([]);
    const [visualizedServers, setVisualizedServers] = useState([]);

    const seePerformance = hasPermission('page.network.performance');

    useEffect(() => {
        if (networkMetadata) {
            const options = networkMetadata.servers;
            setServerOptions(options);

            const indexOfProxy = options
                .findIndex(option => option.serverName === networkMetadata.currentServer.serverName);

            setSelectedOptions([indexOfProxy]);
            setVisualizedServers([indexOfProxy]);
        }
    }, [networkMetadata, setVisualizedServers]);

    const applySelected = () => {
        setVisualizedServers(selectedOptions);
    }

    const [performanceData, setPerformanceData] = useState({});
    const loadPerformanceData = useCallback(async () => {
        if (!seePerformance) return;
        const loaded = {
            servers: [],
            data: [],
            values: [],
            errors: [],
            zones: {},
            colors: {},
            timestamp_f: ''
        }
        const time = new Date().getTime();

        for (const index of visualizedServers) {
            const server = serverOptions[index];

            const {data, error} = await fetchOptimizedPerformance(time, encodeURIComponent(server.serverUUID));
            if (data) {
                loaded.servers.push(server);
                const values = data.values;
                delete data.values;
                loaded.data.push(data);
                loaded.values.push(await mapPerformanceDataToSeries(values));
                loaded.zones = data.zones;
                loaded.colors = data.colors;
                loaded.timestamp_f = data.timestamp_f;
            } else if (error) {
                loaded.errors.push(error);
            }
        }

        const selectedUUIDs = visualizedServers
            .map(index => serverOptions[index])
            .map(server => server.serverUUID);
        const {data, error} = await fetchNetworkPerformanceOverview(time, selectedUUIDs);
        if (error) loaded.errors.push(error);

        setPerformanceData({...loaded, overview: data});
    }, [visualizedServers, serverOptions, setPerformanceData, seePerformance])

    useEffect(() => {
        loadPerformanceData();
    }, [loadPerformanceData, visualizedServers, updateRequested]);

    const isUpToDate = selectedOptions.length === visualizedServers.length && selectedOptions.every(
        (s, i) => s === visualizedServers[i]);
    return (
        <LoadIn>
            {seePerformance && <section className={"network-performance"}>
                <ExtendableRow id={'row-network-performance-0'}>
                    <Col>
                        <PerformanceGraphsCard data={performanceData}/>
                    </Col>
                </ExtendableRow>
                <ExtendableRow id={'row-network-performance-1'}>
                    <Col md={8}>
                        <PerformanceAsNumbersCard data={performanceData?.overview?.numbers}
                                                  servers={performanceData.servers || []}/>
                    </Col>
                    <Col md={4}>
                        <Card>
                            <CardHeader icon={faServer} color={'servers'} label={t('html.label.serverSelector')}/>
                            <MultiSelect options={serverOptions.map(server => server.serverName)}
                                         selectedIndexes={selectedOptions}
                                         setSelectedIndexes={setSelectedOptions}/>
                            <ActionButton onClick={applySelected} disabled={isUpToDate}>
                                {t('html.label.apply')}
                            </ActionButton>
                        </Card>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
};

export default NetworkPerformance