import React, {useState} from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faServer, faSignal, faSkull} from "@fortawesome/free-solid-svg-icons";
import {faCalendarPlus, faClock, faMap} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../Datapoint";
import {Col, Row} from "react-bootstrap-v5";

// TODO Fix animations
// TODO World pie
// TODO Kills table

const SessionHeader = ({session, onClick}) => {
    let style = session.start.includes("Online") ? 'bg-teal' : 'bg-teal-outline';
    return (
        <tr id="session_h_${i}" aria-controls="session_t_${i}" aria-expanded="false"
            className={"clickable collapsed " + style} data-bs-target="#session_t_${i}" data-bs-toggle="collapse"
            onClick={onClick}
        >
            <td>{session.name}{session.first_session ?
                <Fa icon={faCalendarPlus} title="Registered (First session)"/> : ''}</td>
            <td>{session.start}</td>
            <td>{session.length}</td>
            <td>{session.network_server ? session.network_server : session.most_used_world}</td>
        </tr>
    )
}

const SessionBody = ({session, open}) => {
    return (
        <tr className={"collapse" + (open ? ' show' : '')} data-bs-parent="#tableAccordion" id="session_t_${i}">
            <td colSpan="4">
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
                    </Col>
                    <div className="col-xs-12  col-sm-12 col-md-6 col-lg-6">
                        <div id="worldpie_${i}" className="chart-pie"></div>
                        <a href="${session.network_server ? `./player/` : `../player/`}${session.player_uuid}"
                           className="float-end btn bg-blue">
                            <i className="fa fa-user"></i><span> Player Page</span>
                        </a>
                        {session.network_server ? `<a href="./server/${session.server_url_name}" class="float-end btn bg-light-green me-2">
                                <i class="fa fa-server"></i><span> Server Analysis</span>
                            </a>` : ``}
                    </div>
                </Row>
            </td>
        </tr>
    )
}

const Session = (
    {
        session, open, onClick
    }
) => {
    return (
        <>
            <SessionHeader session={session} onClick={onClick}/>
            <SessionBody session={session} open={open}/>
        </>
    )
}

const SessionAccordion = (
    {
        sessions
    }
) => {
    const [openSession, setOpenSession] = useState(0);

    const toggleSession = (i) => {
        setOpenSession(openSession === i ? -1 : i);
    }

    return (
        <table className="table accordion-striped" id="tableAccordion">
            <thead>
            <tr>
                <th><Fa icon={faServer}/> Server</th>
                <th><Fa icon={faClock}/> Session Started</th>
                <th><Fa icon={faClock}/> Length</th>
                <th><Fa icon={faMap}/> Most played World</th>
            </tr>
            </thead>
            <tbody>
            {sessions.map((session, i) => <Session key={'session-' + i} session={session}
                                                   open={openSession === i}
                                                   onClick={() => toggleSession(i)}/>)}
            </tbody>
        </table>
    )
}

export default SessionAccordion