import React from 'react';
import { Card } from 'react-bootstrap';
import CardHeader from '../CardHeader.tsx';
import {
    faCalendar,
    faCalendarCheck,
    faClock,
} from '@fortawesome/free-regular-svg-icons';
import Datapoint from '../../datapoint/Datapoint.tsx';
import { useTranslation } from 'react-i18next';

const SessionsWithinViewCard = ({ data }) => {
    const { t } = useTranslation();

    return (
        <Card>
            <CardHeader
                label={'html.query.title.sessionsWithinView'}
                color={'sessions'}
                icon={faCalendar}
            />
            <Card.Body>
                <Datapoint
                    color={'sessions'}
                    icon={faCalendarCheck}
                    name={t('html.label.sessions')}
                    value={data.totalSessions}
                    bold
                />
                <Datapoint
                    color={'sessions'}
                    icon={faCalendarCheck}
                    name={
                        t('html.label.averageSessions') +
                        ' ' +
                        t('html.label.perPlayer')
                    }
                    value={data.averageSessions}
                    bold
                />
                <Datapoint
                    color={'sessions'}
                    icon={faClock}
                    name={t('html.label.averageSessionLength')}
                    value={data.averageSessionLength}
                />
                <hr />
                <Datapoint
                    color={'playtime'}
                    icon={faClock}
                    name={t('html.label.playtime')}
                    value={data.totalPlaytime}
                />
                <Datapoint
                    color={'playtime-active'}
                    icon={faClock}
                    name={t('html.label.activePlaytime')}
                    value={data.totalActivePlaytime}
                />
                <Datapoint
                    color={'playtime-afk'}
                    icon={faClock}
                    name={t('html.label.afkTime')}
                    value={data.totalAfkPlaytime}
                />
                <hr />
                <Datapoint
                    color={'playtime'}
                    icon={faClock}
                    name={t('html.label.averagePlaytime')}
                    value={data.averagePlaytime}
                />
                <Datapoint
                    color={'playtime-active'}
                    icon={faClock}
                    name={t('html.label.averageActivePlaytime')}
                    value={data.averageActivePlaytime}
                />
                <Datapoint
                    color={'playtime-afk'}
                    icon={faClock}
                    name={t('html.label.averageAfkTime')}
                    value={data.averageAfkPlaytime}
                />
            </Card.Body>
        </Card>
    );
};

export default SessionsWithinViewCard;
