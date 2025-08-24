import React from 'react';
import {Card} from "react-bootstrap";
import CardHeader from "../CardHeader";
import {faCalendar, faCalendarCheck, faClock} from "@fortawesome/free-regular-svg-icons";
import Datapoint from "../../Datapoint";
import {useTranslation} from "react-i18next";

const SessionsWithinViewCard = ({data}) => {
    const {t} = useTranslation();

    return (
        <Card>
            <CardHeader label={'html.query.title.sessionsWithinView'} color={'sessions'} icon={faCalendar}/>
            <Card.Body>
                <Datapoint color={'sessions'} icon={faCalendarCheck} name={t('html.label.sessions')}
                           value={data.total_sessions} bold/>
                <Datapoint color={'sessions'} icon={faCalendarCheck}
                           name={t('html.label.averageSessions') + ' ' + t('html.label.perPlayer')}
                           value={data.average_sessions} bold/>
                <Datapoint color={'sessions'} icon={faClock} name={t('html.label.averageSessionLength')}
                           value={data.average_session_length}/>
                <hr/>
                <Datapoint color={'playtime'} icon={faClock} name={t('html.label.playtime')}
                           value={data.total_playtime}/>
                <Datapoint color={'playtime-active'} icon={faClock} name={t('html.label.activePlaytime')}
                           value={data.total_active_playtime}/>
                <Datapoint color={'playtime-afk'} icon={faClock} name={t('html.label.afkTime')}
                           value={data.total_afk_playtime}/>
                <hr/>
                <Datapoint color={'playtime'} icon={faClock} name={t('html.label.averagePlaytime')}
                           value={data.average_playtime}/>
                <Datapoint color={'playtime-active'} icon={faClock} name={t('html.label.averageActivePlaytime')}
                           value={data.average_active_playtime}/>
                <Datapoint color={'playtime-afk'} icon={faClock} name={t('html.label.averageAfkTime')}
                           value={data.average_afk_playtime}/>
            </Card.Body>
        </Card>
    )
};

export default SessionsWithinViewCard