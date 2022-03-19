import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendar, faCalendarAlt, faClock, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import PlayerSessionCalendar from "../components/calendar/PlayerSessionCalendar";
import Scrollable from "../components/Scrollable";
import SessionAccordion from "../components/accordion/SessionAccordion";
import WorldPie from "../components/graphs/WorldPie";
import {usePlayer} from "./PlayerPage";

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

const WorldPieCard = ({player}) => (
    <Card>
        <Card.Header>
            <h6 className="col-black" style={{width: '100%'}}>
                <Fa icon={faClock} className="col-teal"/> World Playtime
            </h6>
        </Card.Header>
        <WorldPie
            id="world-pie"
            worldSeries={player.world_pie_series}
            gmSeries={player.gm_series}
        />
    </Card>
)

const PlayerSessions = () => {
    const {player} = usePlayer();
    return (
        <section className="player_sessions">
            <Row>
                <Col lg={8}>
                    <SessionCalendarCard player={player}/>
                    <RecentSessionsCard player={player}/>
                </Col>
                <Col lg={4}>
                    <WorldPieCard player={player}/>
                </Col>
            </Row>
        </section>
    )
}

export default PlayerSessions