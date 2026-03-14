import React from "react";
import {Card, Col} from "react-bootstrap";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {faCalendar, faHandPointer} from "@fortawesome/free-regular-svg-icons";
import PlayerSessionCalendar from "../../components/calendar/PlayerSessionCalendar";
import {usePlayer} from "../layout/PlayerPage";
import {useTranslation} from "react-i18next";
import PlayerWorldPieCard from "../../components/cards/player/PlayerWorldPieCard";
import PlayerRecentSessionsCard from "../../components/cards/player/PlayerRecentSessionsCard";
import LoadIn from "../../components/animation/LoadIn.tsx";
import ExtendableRow from "../../components/layout/extension/ExtendableRow";
import {useAuth} from "../../hooks/authenticationHook.tsx";
import {GenericFilterContextProvider} from "../../dataHooks/genericFilterContextHook.tsx";
import {DateFilterControls} from "../../components/input/DateFilterControls.tsx";
import {staticSite} from "../../service/backendConfiguration.js";
import CardHeader from "../../components/cards/CardHeader.tsx";

const SessionCalendarCard = ({player}) => {
    const {t} = useTranslation();
    return (
        <Card>
            <CardHeader icon={faCalendar} color={"sessions"} label={'html.label.sessionCalendar'}>
                {!staticSite && <span className="float-end">
                <FontAwesomeIcon icon={faHandPointer}/>{' '}<small>{t('html.text.clickAndDrag')}</small>
            </span>}
            </CardHeader>
            <PlayerSessionCalendar series={player.calendar_series} firstDay={player.first_day}/>
        </Card>
    )
}

const PlayerSessions = () => {
    const {hasPermission} = useAuth();
    const {player} = usePlayer();
    return (
        <LoadIn>
            {hasPermission('page.player.sessions') &&
                <GenericFilterContextProvider initialValue={{player: player.info.uuid}}>
                    <section className="player-sessions" id={"player-sessions"}>
                        <ExtendableRow id={'row-player-sessions-0'}>
                            <Col lg={6}>
                                <SessionCalendarCard player={player}/>
                            </Col>
                            <Col lg={6}>
                                <DateFilterControls/>
                                <PlayerRecentSessionsCard player={player}/>
                                <PlayerWorldPieCard player={player}/>
                            </Col>
                        </ExtendableRow>
                    </section>
                </GenericFilterContextProvider>}
        </LoadIn>
    )
}

export default PlayerSessions;