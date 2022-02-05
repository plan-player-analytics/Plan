import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faGavel, faLocationArrow, faServer, faSkull} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck, faCalendarPlus, faClock} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../Datapoint";
import {Col, Row} from "react-bootstrap-v5";
import WorldPie from "../graphs/WorldPie";
import Accordion from "./Accordion";
import {faSuperpowers} from "@fortawesome/free-brands-svg-icons";

const ServerHeader = ({server}) => {
    return (
        <>
            <td>{server.server_name}
                {server.operator ? <Fa icon={faSuperpowers} title="Operator"/> : ''}
                {server.banned ? <Fa icon={faGavel} title="Banned"/> : ''}
            </td>
            <td>{server.playtime}</td>
            <td>{server.registered}</td>
            <td>{server.last_seen}</td>
        </>
    )
}

const ServerBody = ({i, server}) => {
    return (
        <Row>
            <Col lg={6}>
                {server.operator ? <Datapoint icon={faSuperpowers} color="blue" name="Operator"/> : ''}
                {server.banned ? <Datapoint icon={faGavel} color="red" name="Banned"/> : ''}
                {server.operator || server.banned ? <br/> : ''}
                <Datapoint
                    icon={faCalendarCheck} color={"teal"}
                    name="Sessions" value={server.session_count} bold
                />
                <Datapoint
                    icon={faClock} color={"green"}
                    name="Playtime" value={server.playtime} bold
                />
                <Datapoint
                    icon={faClock} color={"grey"}
                    name="AFK Time" value={server.afk_time} bold
                />
                <Datapoint
                    icon={faClock} color={"teal"}
                    name="Longest Session" value={server.longest_session_length} bold
                />
                <Datapoint
                    icon={faClock} color={"teal"}
                    name="Session Median" value={server.session_median} bold
                />
                <br/>
                <Datapoint
                    icon={faLocationArrow} color={"amber"}
                    name="Join Address" value={server.join_address}
                />
                <br/>
                <Datapoint
                    icon={faCrosshairs} color="red"
                    name="Player Kills" value={server.player_kills} bold
                />
                <Datapoint
                    icon={faCrosshairs} color="green"
                    name="Mob Kills" value={server.mob_kills} bold
                />
                <Datapoint
                    icon={faSkull} color="black"
                    name="Deaths" value={server.deaths} bold
                />
                <hr/>
            </Col>
            <div className="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                <WorldPie id={"worldpie_server_" + i}
                          worldSeries={server.world_pie_series}
                          gmColors={[]}
                          gmSeries={server.gm_series}/>
            </div>
        </Row>
    )
}

const ServerAccordion = ({servers}) => {
    return (
        <Accordion headers={[
            <><Fa icon={faServer}/> Server</>,
            <><Fa icon={faClock}/> Playtime</>,
            <><Fa icon={faCalendarPlus}/> Registered</>,
            <><Fa icon={faCalendarCheck}/> Last Seen</>
        ]} slices={servers.map(server => {
            return {
                body: <ServerBody server={server}/>,
                header: <ServerHeader server={server}/>,
                color: 'light-green',
                outline: true
            }
        })}/>
    )
}

export default ServerAccordion