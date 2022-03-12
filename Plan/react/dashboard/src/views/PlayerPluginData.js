import React, {useEffect} from "react";
import ExtensionCard, {ExtensionCardWrapper} from "../components/extensions/ExtensionCard";
import {Card, Row} from "react-bootstrap-v5";
import {useParams} from "react-router-dom";
import Masonry from "masonry-layout";
import {usePlayer} from "./PlayerPage";

const Header = ({player, extension_data}) => (
    <div className="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 className="h3 mb-0 text-gray-800">
            {player.info.name} &middot; Plugins Overview ({extension_data.serverName})
        </h1>
    </div>
)

const PlayerPluginData = () => {
    const {player} = usePlayer();
    const {serverName} = useParams();

    const extensions = player.extensions.find(extension => extension.serverName === serverName)

    useEffect(() => {
        const masonryRow = document.getElementById('extension-masonry-row');
        let masonry = Masonry.data(masonryRow);
        if (!masonry) {
            masonry = new Masonry(masonryRow, {"percentPosition": true, "itemSelector": ".extension-wrapper"});
        }
        return () => {
            if (masonry.element) masonry.destroy();
        }
    }, [])

    if (!extensions) {
        return <section className="player_plugin_data">
            <Header player={player} extension_data={{serverName}}/>
            <Row style={{overflowY: 'hidden'}}>
                <Card>
                    <Card.Body>
                        <p>No Extension data for {serverName}</p>
                    </Card.Body>
                </Card>
            </Row>
        </section>
    }

    return (
        <section className="player_plugin_data">
            <Header player={player} extension_data={extensions}/>
            <Row id="extension-masonry-row"
                 data-masonry='{"percentPosition": true, "itemSelector": ".extension-wrapper"}'
                 style={{overflowY: 'hidden'}}>
                {extensions.extensionData.map((extension, i) =>
                    <ExtensionCardWrapper key={'ext-' + i} extension={extension}>
                        <ExtensionCard extension={extension}/>
                    </ExtensionCardWrapper>
                )}
            </Row>
        </section>
    )
}

export default PlayerPluginData;