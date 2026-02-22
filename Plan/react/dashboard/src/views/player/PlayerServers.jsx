import React from "react";
import {Card, Col} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faHandPointer} from "@fortawesome/free-regular-svg-icons";
import Scrollable from "../../components/Scrollable";
import {faNetworkWired, faSignal} from "@fortawesome/free-solid-svg-icons";
import ServerPie from "../../components/graphs/ServerPie";
import ServerAccordion from "../../components/accordion/ServerAccordion";
import {usePlayer} from "../layout/PlayerPage";
import {useTranslation} from "react-i18next";
import PlayerPingGraph from "../../components/graphs/PlayerPingGraph";
import LoadIn from "../../components/animation/LoadIn.tsx";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const PingGraphCard = ({player}) => {
    const {t} = useTranslation();

    const hasPingData = Boolean(player.ping_graph.avg_ping_series.length);

    return (
        <Card>
            <Card.Header>
                <h6 className="col-text" style={{width: '100%'}}>
                    <Fa icon={faSignal} className="col-ping"/> {t('html.label.ping')}
                </h6>
            </Card.Header>
            {hasPingData && <PlayerPingGraph data={player.ping_graph}/>}
            {!hasPingData && <Card.Body><p>{t('generic.noData')}</p></Card.Body>}
        </Card>
    )
}

const ServersCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-text" style={{width: '100%'}}>
                    <Fa icon={faNetworkWired} className="col-servers"/> {t('html.label.servers')}
                    <span className="float-end">
                    <Fa icon={faHandPointer}/> <small>{t('html.text.clickToExpand')}</small>
                </span>
                </h6>
            </Card.Header>
            <Scrollable>
                <ServerAccordion servers={player.servers}/>
            </Scrollable>
        </Card>
    )
}

const ServerPieCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-text" style={{width: '100%'}}>
                    <Fa icon={faNetworkWired} className="col-sessions"/> {t('html.label.serverPlaytime')}
                </h6>
            </Card.Header>
            <ServerPie
                colors={player.server_pie_colors}
                series={player.server_pie_series}
            />
        </Card>
    )
}


const PlayerServers = () => {
    const {hasPermission} = useAuth();
    const {player} = usePlayer();
    return (
        <LoadIn>
            {hasPermission('page.player.servers') && <section className="player-servers" id={"player-servers"}>
                <ExtendableRow id={'row-player-servers-0'}>
                    <Col lg={12}>
                        <PingGraphCard player={player}/>
                    </Col>
                </ExtendableRow>
                <ExtendableRow id={'row-player-servers-1'}>
                    <Col lg={8}>
                        <ServersCard player={player}/>
                    </Col>
                    <Col lg={4}>
                        <ServerPieCard player={player}/>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
}

export default PlayerServers