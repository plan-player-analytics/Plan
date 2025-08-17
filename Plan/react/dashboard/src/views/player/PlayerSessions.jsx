import React from "react";
import {Card, Col} from "react-bootstrap";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCalendarAlt} from "@fortawesome/free-regular-svg-icons";
import PlayerSessionCalendar from "../../components/calendar/PlayerSessionCalendar";
import {usePlayer} from "../layout/PlayerPage";
import {useTranslation} from "react-i18next";
import PlayerWorldPieCard from "../../components/cards/player/PlayerWorldPieCard";
import PlayerRecentSessionsCard from "../../components/cards/player/PlayerRecentSessionsCard";
import LoadIn from "../../components/animation/LoadIn";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook";

const SessionCalendarCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className="col-text">
                    <Fa icon={faCalendarAlt} className="col-sessions"/> {t('html.label.sessionCalendar')}
                </h6>
            </Card.Header>
            <PlayerSessionCalendar series={player.calendar_series} firstDay={player.first_day}/>
        </Card>
    )
}

const PlayerSessions = () => {
    const {hasPermission} = useAuth();
    const {player} = usePlayer();
    return (
        <LoadIn>
            {hasPermission('page.player.sessions') && <section className="player-sessions" id={"player-sessions"}>
                <ExtendableRow id={'row-player-sessions-0'}>
                    <Col lg={8}>
                        <SessionCalendarCard player={player}/>
                        <PlayerRecentSessionsCard player={player}/>
                    </Col>
                    <Col lg={4}>
                        <PlayerWorldPieCard player={player}/>
                    </Col>
                </ExtendableRow>
            </section>}
        </LoadIn>
    )
}

export default PlayerSessions;