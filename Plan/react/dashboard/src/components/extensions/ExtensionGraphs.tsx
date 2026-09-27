import React from "react";
import {ExtensionTab} from "../../dataHooks/model/extension/ExtensionData";
import {Col} from "react-bootstrap";
import {ExtensionGraph} from "./ExtensionGraph";
import {useParams} from "react-router";
import {usePlayer} from "../../views/layout/PlayerPage";
import {useMetadata} from "../../hooks/metadataHook";
import {CardSection} from "../cards/CardSection";

type Props = {
    uuid: string;
    tab: ExtensionTab;
    width: number;
}

export const ExtensionGraphs = ({uuid, tab, width}: Props) => {
    const metadata = useMetadata();

    if (!metadata?.loaded || !metadata.networkMetadata) return null;

    const {servers} = metadata.networkMetadata;
    const {serverUUID: networkUUID} = metadata;
    const {identifier, serverName} = useParams();
    const playerPayload = usePlayer() as { player?: { info: { uuid: string } } };
    const player = playerPayload?.player || undefined;

    const server = player ? serverName : (identifier || networkUUID);
    const serverUUID = servers.find(s => s.serverName === server || s.serverUUID === server)?.serverUUID;

    if (!server) return null;

    return (<>
        {tab.graphNames.map(graphName => (
            <Col key={graphName} md={width} className={"extension-section-wrapper-" + uuid}>
                <CardSection noPadding>
                    <ExtensionGraph graph={graphName} server={serverUUID || server} player={player?.info.uuid}/>
                </CardSection>
            </Col>
        ))}
    </>);
}