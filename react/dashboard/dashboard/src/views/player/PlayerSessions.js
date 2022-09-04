import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendarAlt} from "@fortawesome/free-regular-svg-icons";
import PlayerSessionCalendar from "../../components/calendar/PlayerSessionCalendar";
import {usePlayer} from "../layout/PlayerPage";
import {useTranslation} from "react-i18next";
import PlayerWorldPieCard from "../../components/cards/player/PlayerWorldPieCard";
import PlayerRecentSessionsCard from "../../components/cards/player/PlayerRecentSessionsCard";
import LoadIn from "../../components/animation/LoadIn";

const SessionCalendarCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black">
                    <Fa icon={faCalendarAlt} className="col-teal"/> {t('html.label.sessionCalendar')}
                </h6>
            </Card.Header>
            <PlayerSessionCalendar series={player.calendar_series} firstDay={player.first_day}/>
        </Card>
    )
}

const PlayerSessions = () => {
    const {player} = usePlayer();
    return (
        <LoadIn>
            <section className="player_sessions">
                <Row>
                    <Col lg={8}>
                        <SessionCalendarCard player={player}/>
                        <PlayerRecentSessionsCard player={player}/>
                    </Col>
                    <Col lg={4}>
                        <PlayerWorldPieCard player={player}/>
                    </Col>
                </Row>
            </section>
        </LoadIn>
    )
}

export default PlayerSessions;