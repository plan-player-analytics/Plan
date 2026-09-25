import React from "react";
import {ExtensionTab} from "../../dataHooks/model/extension/ExtensionData";
import {Col} from "react-bootstrap";
import {ExtensionGraph} from "./ExtensionGraph";
import {useParams} from "react-router";
import {usePlayer} from "../../views/layout/PlayerPage";
import {useMetadata} from "../../hooks/metadataHook";

type Props = {
    tab: ExtensionTab;
    width: number;
}

export const ExtensionGraphs = ({tab, width}: Props) => {
    const metadata = useMetadata();

    if (!metadata?.loaded || !metadata.networkMetadata) return null;

    const {servers} = metadata.networkMetadata;
    const {identifier, serverName} = useParams();
    const {player} = usePlayer() as { player?: string };

    const server = player ? serverName : identifier;
    const serverUUID = servers.find(s => s.serverName === server)?.serverUUID;

    if (!server) return null;

    return (<>
        {tab.graphNames.map(graphName => (
            <Col key={graphName} md={width} className="extension-section-wrapper">
                <ExtensionGraph graph={graphName} server={serverUUID || server} player={player}/>
            </Col>
        ))}
    </>);
}