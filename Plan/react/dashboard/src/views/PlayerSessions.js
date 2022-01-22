import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendar, faCalendarAlt, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import PlayerSessionCalendar from "../components/calendar/PlayerSessionCalendar";
import Scrollable from "../components/Scrollable";
import SessionAccordion from "../components/accordion/SessionAccordion";

const Header = ({player}) => (
    <div className="d-sm-flex align-items-center justify-content-between mb-4">
        <h1 className="h3 mb-0 text-gray-800">
            {player.info.name} &middot; Sessions
        </h1>
    </div>
)

const SessionCalendarCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black">
                <Fa icon={faCalendarAlt} className="col-teal"/> Session Calendar
            </h6>
        </Card.Header>
        <PlayerSessionCalendar series={player.calendar_series} firstDay={player.first_day}/>
    </Card>
)

const RecentSessionsCard = ({player}) => (
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
            <SessionAccordion sessions={player.sessions}/>
        </Scrollable>
    </Card>
)

const PlayerSessions = ({player}) => {
    return (
        <section className="player_sessions">
            <Header player={player}/>
            <Row>
                <Col lg={8}>
                    <SessionCalendarCard player={player}/>
                    <RecentSessionsCard player={player}/>
                </Col>
            </Row>
        </section>
    )
}

export default PlayerSessions