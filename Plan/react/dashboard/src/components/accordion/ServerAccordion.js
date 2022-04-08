import React from "react";
import {FontAwesomeIcon as Fa} from "@fortawesome/react-fontawesome";
import {faCrosshairs, faGavel, faLocationArrow, faServer, faSkull} from "@fortawesome/free-solid-svg-icons";
import {faCalendarCheck, faCalendarPlus, faClock} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../Datapoint";
import {Col, Row} from "react-bootstrap-v5";
import WorldPie from "../graphs/WorldPie";
import Accordion from "./Accordion";
import {faSuperpowers} from "@fortawesome/free-brands-svg-icons";
import {useTranslation} from "react-i18next";
import {baseAddress} from "../../service/backendConfiguration";
import {useAuth} from "../../hooks/authenticationHook";

const ServerHeader = ({server}) => {
    return (
        <>
            <td>{server.server_name}
                {server.operator ? <Fa icon={faSuperpowers} title="Operator"/> : ''}
                {server.banned ? <Fa icon={faGavel} title="Banned"/> : ''}
            </td>
            <td>{server.playtime}</td>
            <td>{server.registered}</td>
            <td>{server.last_seen}</td>
        </>
    )
}

const ServerBody = ({i, server}) => {
    const {t} = useTranslation();
    const {hasPermission} = useAuth();

    return (
        <Row>
            <Col lg={6}>
                {server.operator ? <Datapoint icon={faSuperpowers} color="blue" name={t('html.label.operator')}/> : ''}
                {server.banned ? <Datapoint icon={faGavel} color="red" name={t('html.label.banned')}/> : ''}
                {server.operator || server.banned ? <br/> : ''}
                <Datapoint
                    icon={faCalendarCheck} color={"teal"}
                    name={t('html.label.sessions')} value={server.session_count} bold
                />
                <Datapoint
                    icon={faClock} color={"green"}
                    name={t('html.label.playtime')} value={server.playtime} bold
                />
                <Datapoint
                    icon={faClock} color={"grey"}
                    name={t('html.label.afkTime')} value={server.afk_time} bold
                />
                <Datapoint
                    icon={faClock} color={"teal"}
                    name={t('html.label.longestSession')} value={server.longest_session_length} bold
                />
                <Datapoint
                    icon={faClock} color={"teal"}
                    name={t('html.label.sessionMedian')} value={server.session_median} bold
                />
                <br/>
                <Datapoint
                    icon={faLocationArrow} color={"amber"}
                    name={t('html.label.joinAddress')} value={server.join_address}
                />
                <br/>
                <Datapoint
                    icon={faCrosshairs} color="red"
                    name={t('html.label.playerKills')} value={server.player_kills} bold
                />
                <Datapoint
                    icon={faCrosshairs} color="green"
                    name={t('html.label.mobKills')} value={server.mob_kills} bold
                />
                <Datapoint
                    icon={faSkull} color="black"
                    name={t('html.label.deaths')} value={server.deaths} bold
                />
                <hr/>
            </Col>
            <div className="col-xs-12 col-sm-12 col-md-12 col-lg-6">
                <WorldPie id={"worldpie_server_" + i}
                          worldSeries={server.world_pie_series}
                          gmSeries={server.gm_series}/>

                {hasPermission('page.server') && <a href={`${baseAddress}/server/${server.server_uuid}`}
                                                    className="float-end btn bg-light-green me-2">
                    <Fa icon={faServer}/> {t('html.label.serverPage')}
                </a>}
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
                color: 'light-green',
                outline: true
            }
        })}/>
    )
}

export default ServerAccordion