import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faServer, faSignal, faSkull, faUser, faUserPlus} from "@fortawesome/free-solid-svg-icons";
import {faClock, faMap} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../Datapoint";
import {Col, Row} from "react-bootstrap";
import WorldPie from "../graphs/WorldPie";
import {SimpleKillsTable} from "../table/KillsTable";
import Accordion from "./Accordion";
import {useTranslation} from "react-i18next";
import {ChartLoader} from "../navigation/Loader";
import {usePreferences} from "../../hooks/preferencesHook.jsx";
import FormattedDate from "../text/FormattedDate.jsx";
import FormattedTime from "../text/FormattedTime.jsx";
import {formatDecimals} from "../../util/formatters.js";
import PlayerPageLinkButton from "../input/button/PlayerPageLinkButton.jsx";
import ServerPageLinkButton from "../input/button/ServerPageLinkButton.jsx";

const SessionHeader = ({session}) => {
    const {t} = useTranslation();
    return (
        <>
            <td>{session.name} {session.first_session ?
                <Fa icon={faUserPlus} title="Registered (First session)"/> : ''}</td>
            <td><FormattedDate date={session.start}/>{session.online ? ` (${t('html.value.online').trim()})` : ''}</td>
            <td><FormattedTime timeMs={session.length}/></td>
            <td>{session.network_server ? session.network_server : session.most_used_world}</td>
        </>
    )
}

const SessionBody = ({i, session}) => {
    const {t} = useTranslation();
    const {decimalFormat} = usePreferences();
    return (
        <Row>
            <Col lg={6}>
                <Datapoint
                    icon={faClock} color={"sessions"}
                    name={t('html.label.sessionEnded')} value={<FormattedDate date={session.end}/>} bold
                />
                <Datapoint
                    icon={faClock} color={"sessions"}
                    name={t('html.label.length')} value={<FormattedTime timeMs={session.length}/>} bold
                />
                <Datapoint
                    icon={faClock} color={"playtime-afk"}
                    name={t('html.label.afkTime')} value={<FormattedTime timeMs={session.afk_time}/>} bold
                />
                <Datapoint
                    icon={faServer} color={"servers"}
                    name={t('html.label.server')} value={session.server_name} bold
                />
                {session.avg_ping ? <Datapoint
                    icon={faSignal} color={"ping"}
                    name={t('html.label.averagePing')} value={formatDecimals(session.avg_ping, decimalFormat)} bold
                /> : ''}
                <br/>
                <Datapoint
                    icon={faCrosshairs} color="player-kills"
                    name={t('html.label.playerKills')} value={session.player_kills.length} bold
                />
                <Datapoint
                    icon={faCrosshairs} color="mob-kills"
                    name={t('html.label.mobKills')} value={session.mob_kills} bold
                />
                <Datapoint
                    icon={faSkull} color="deaths"
                    name={t('html.label.deaths')} value={session.deaths} bold
                />
                <hr/>
                <SimpleKillsTable kills={session.player_kills}/>
            </Col>
            <div className="col-xs-12 col-sm-12 col-md-12 col-lg-6">
                <WorldPie id={"worldpie_" + i}
                          worldSeries={session.world_series}
                          gmSeries={session.gm_series}/>
                <PlayerPageLinkButton uuid={session.player_uuid} className={'float-end'}/>
                <ServerPageLinkButton uuid={session.server_uuid} className={'float-end me-2'}/>
            </div>
        </Row>
    )
}

const SessionAccordion = (
    {
        sessions,
        isPlayer,
        isNetwork
    }
) => {
    const {t} = useTranslation();
    const {preferencesLoaded} = usePreferences();

    if (!sessions || !preferencesLoaded) return <ChartLoader/>

    const firstColumn = isPlayer ? (<><Fa icon={faUser}/> {t('html.label.player')}</>)
        : (<><Fa icon={faServer}/> {t('html.label.server')}</>)

    return (
        <Accordion headers={[
            firstColumn,
            <><Fa icon={faClock}/> {t('html.label.sessionStart')}</>,
            <><Fa icon={faClock}/> {t('html.label.length')}</>,
            <>
                {!isNetwork && <><Fa icon={faMap}/> {t('html.label.mostPlayedWorld')}</>}
                {isNetwork && <><Fa icon={faServer}/> {t('html.label.server')}</>}
            </>
        ]} slices={sessions.map(session => {
            return {
                body: <SessionBody session={session}/>,
                header: <SessionHeader session={session}/>,
                color: 'sessions',
                outline: !session.online
            }
        })}/>
    )
}

export default SessionAccordion