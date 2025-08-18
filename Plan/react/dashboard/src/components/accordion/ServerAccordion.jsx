import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faGavel, faLocationArrow, faServer, faSkull} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck, faCalendarPlus, faClock} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../Datapoint";
import {Col, Row} from "react-bootstrap";
import WorldPie from "../graphs/WorldPie";
import Accordion from "./Accordion";
import {faSuperpowers} from "@fortawesome/free-brands-svg-icons";
import {useTranslation} from "react-i18next";
import FormattedDate from "../text/FormattedDate.jsx";
import FormattedTime from "../text/FormattedTime.jsx";
import ServerPageLinkButton from "../input/button/ServerPageLinkButton.jsx";

const ServerHeader = ({server}) => {
    const {t} = useTranslation();
    return (
        <>
            <td>{t(server.server_name)}
                {server.operator ? <span title={t('html.label.operator')}><Fa icon={faSuperpowers}/></span> : ''}
                {server.banned ? <span title={t('html.label.banned')}><Fa icon={faGavel}/></span> : ''}
            </td>
            <td><FormattedTime timeMs={server.playtime}/></td>
            <td><FormattedDate date={server.registered}/></td>
            <td><FormattedDate date={server.last_seen}/></td>
        </>
    )
}

const ServerBody = ({i, server}) => {
    const {t} = useTranslation();

    return (
        <Row>
            <Col lg={6}>
                {server.operator ?
                    <Datapoint icon={faSuperpowers} color="operator" name={t('html.label.operator')}/> : ''}
                {server.banned ? <Datapoint icon={faGavel} color="banned" name={t('html.label.banned')}/> : ''}
                {server.operator || server.banned ? <br/> : ''}
                <Datapoint
                    icon={faCalendarCheck} color={"sessions"}
                    name={t('html.label.sessions')} value={server.session_count} bold
                />
                <Datapoint
                    icon={faClock} color={"playtime"}
                    name={t('html.label.playtime')} value={<FormattedTime timeMs={server.playtime}/>} bold
                />
                <Datapoint
                    icon={faClock} color={"playtime-afk"}
                    name={t('html.label.afkTime')} value={<FormattedTime timeMs={server.afk_time}/>} bold
                />
                <Datapoint
                    icon={faClock} color={"sessions"}
                    name={t('html.label.longestSession')}
                    value={<FormattedTime timeMs={server.longest_session_length}/>} bold
                />
                <Datapoint
                    icon={faClock} color={"sessions"}
                    name={t('html.label.sessionMedian')} value={<FormattedTime timeMs={server.session_median}/>} bold
                />
                <br/>
                <Datapoint
                    icon={faLocationArrow} color={"join-addresses"}
                    name={t('html.label.joinAddress')} value={server.join_address}
                />
                <br/>
                <Datapoint
                    icon={faCrosshairs} color="player-kills"
                    name={t('html.label.playerKills')} value={server.player_kills} bold
                />
                <Datapoint
                    icon={faCrosshairs} color="mob-kills"
                    name={t('html.label.mobKills')} value={server.mob_kills} bold
                />
                <Datapoint
                    icon={faSkull} color="deaths"
                    name={t('html.label.deaths')} value={server.deaths} bold
                />
                <hr/>
            </Col>
            <div className="col-xs-12 col-sm-12 col-md-12 col-lg-6">
                <WorldPie id={"worldpie_server_" + i}
                          worldSeries={server.world_pie_series}
                          gmSeries={server.gm_series}/>

                <ServerPageLinkButton uuid={server.server_uuid} className={'float-end'}/>
            </div>
        </Row>
    )
}

const ServerAccordion = ({servers}) => {
    const {t} = useTranslation();
    return (
        <Accordion headers={[
            <><Fa icon={faServer}/> {t('html.label.server')}</>,
            <><Fa icon={faClock}/> {t('html.label.playtime')}</>,
            <><Fa icon={faCalendarPlus}/> {t('html.label.registered')}</>,
            <><Fa icon={faCalendarCheck}/> {t('html.label.lastSeen')}</>
        ]} slices={servers.map(server => {
            return {
                body: <ServerBody server={server}/>,
                header: <ServerHeader server={server}/>,
                color: 'servers',
                outline: true
            }
        })}/>
    )
}

export default ServerAccordion