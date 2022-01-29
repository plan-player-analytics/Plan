import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faServer, faSignal, faSkull, faUser} from "@fortawesome/free-solid-svg-icons";
import {faCalendarPlus, faClock, faMap} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../Datapoint";
import {Col, Row} from "react-bootstrap-v5";
import WorldPie from "../graphs/WorldPie";
import KillsTable from "../table/KillsTable";
import Accordion from "./Accordion";

const SessionHeader = ({session}) => {
    return (
        <>
            <td>{session.name}{session.first_session ?
                <Fa icon={faCalendarPlus} title="Registered (First session)"/> : ''}</td>
            <td>{session.start}</td>
            <td>{session.length}</td>
            <td>{session.network_server ? session.network_server : session.most_used_world}</td>
        </>
    )
}

const SessionBody = ({i, session}) => {
    return (
        <Row>
            <Col lg={6}>
                <Datapoint
                    icon={faClock} color={"teal"}
                    name="Ended" value={session.end} bold
                />
                <Datapoint
                    icon={faClock} color={"teal"}
                    name="Length" value={session.length} bold
                />
                <Datapoint
                    icon={faClock} color={"grey"}
                    name="AFK Time" value={session.afk_time} bold
                />
                <Datapoint
                    icon={faServer} color={"green"}
                    name="Server" value={session.server_name} bold
                />
                {session.avg_ping ? <Datapoint
                    icon={faSignal} color={"amber"}
                    name="Average Ping" value={session.avg_ping} bold
                /> : ''}
                <br/>
                <Datapoint
                    icon={faCrosshairs} color="red"
                    name="Player Kills" value={session.player_kills.length} bold
                />
                <Datapoint
                    icon={faCrosshairs} color="green"
                    name="Mob Kills" value={session.mob_kills} bold
                />
                <Datapoint
                    icon={faSkull} color="black"
                    name="Deaths" value={session.deaths} bold
                />
                <hr/>
                <KillsTable kills={session.player_kills}/>
            </Col>
            <div className="col-xs-12 col-sm-12 col-md-6 col-lg-6">
                <WorldPie id={"worldpie_" + i}
                          worldSeries={session.world_series}
                          gmColors={[]}
                          gmSeries={session.gm_series}/>
                <a href={session.network_server ? `./player/` : `../player/` + session.player_uuid}
                   className="float-end btn bg-blue">
                    <Fa icon={faUser}/> Player Page
                </a>
                {session.network_server ? <a href={"./server/" + session.server_url_name}
                                             className="float-end btn bg-light-green me-2">
                    <Fa icon={faServer}/> Server Analysis
                </a> : ''}
            </div>
        </Row>
    )
}

const SessionAccordion = (
    {
        sessions
    }
) => {
    return (
        <Accordion headers={[
            <><Fa icon={faServer}/> Server</>,
            <><Fa icon={faClock}/> Session Started</>,
            <><Fa icon={faClock}/> Length</>,
            <><Fa icon={faMap}/> Most played World</>
        ]} slices={sessions.map(session => {
            return {
                body: <SessionBody session={session}/>,
                header: <SessionHeader session={session}/>,
                color: 'teal',
                outline: !session.start.includes("Online")
            }
        })}/>
    )
}

export default SessionAccordion