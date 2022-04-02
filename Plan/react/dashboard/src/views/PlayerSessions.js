import React from "react";
import {Card, Col, Row} from "react-bootstrap-v5";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendar, faCalendarAlt, faClock, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import PlayerSessionCalendar from "../components/calendar/PlayerSessionCalendar";
import Scrollable from "../components/Scrollable";
import SessionAccordion from "../components/accordion/SessionAccordion";
import WorldPie from "../components/graphs/WorldPie";
import {usePlayer} from "./PlayerPage";
import {useTranslation} from "react-i18next";

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

const RecentSessionsCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black" style={{width: '100%'}}>
                    <Fa icon={faCalendar} className="col-teal"/> {t('html.label.recentSessions')}
                    <span className="float-end">
                    <Fa icon={faHandPointer}/> <small>{t('html.text.clickToExpand')}</small>
                </span>
                </h6>
            </Card.Header>
            <Scrollable>
                <SessionAccordion sessions={player.sessions}/>
            </Scrollable>
        </Card>
    )
}

const WorldPieCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-black" style={{width: '100%'}}>
                    <Fa icon={faClock} className="col-teal"/> {t('html.label.worldPlaytime')}
                </h6>
            </Card.Header>
            <WorldPie
                id="world-pie"
                worldSeries={player.world_pie_series}
                gmSeries={player.gm_series}
            />
        </Card>
    )
}

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