import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendar, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import Scrollable from "../components/Scrollable";
import {faNetworkWired} from "@fortawesome/free-solid-svg-icons";
import ServerPie from "../components/graphs/ServerPie";
import ServerAccordion from "../components/accordion/ServerAccordion";
import {usePlayer} from "./PlayerPage";

const ServersCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black" style={{width: '100%'}}>
                <Fa icon={faCalendar} className="col-teal"/> Recent sessions
                <span className="float-end">
                    <Fa icon={faHandPointer}/> <small>Click to expand</small>
                </span>
            </h6>
        </Card.Header>
        <Scrollable>
            <ServerAccordion servers={player.servers}/>
        </Scrollable>
    </Card>
)

const ServerPieCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black" style={{width: '100%'}}>
                <Fa icon={faNetworkWired} className="col-teal"/> Server Playtime
            </h6>
        </Card.Header>
        <ServerPie
            colors={player.server_pie_colors}
            series={player.server_pie_series}
        />
    </Card>
)


const PlayerServers = () => {
    const {player} = usePlayer();
    return (
        <section className="player_sessions">
            <Row>
                <Col lg={8}>
                    <ServersCard player={player}/>
                </Col>
                <Col lg={4}>
                    <ServerPieCard player={player}/>
                </Col>
            </Row>
        </section>
    )
}

export default PlayerServers