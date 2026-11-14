import { useTranslation } from 'react-i18next';
import { Card } from 'react-bootstrap';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { faExchangeAlt, faUsers } from '@fortawesome/free-solid-svg-icons';
import React from 'react';
import { faClock } from '@fortawesome/free-regular-svg-icons';
import { QueryDatapointTable } from '../../../table/QueryDatapointTable';
import { DatapointType } from '../../../../dataHooks/model/datapoint/Datapoint';

type Props = {
    identifier?: string;
};

const PlayerbaseTrendsCard = ({ identifier }: Props) => {
    const { t } = useTranslation();
    return (
        <Card id={'playerbase-trends'}>
            <Card.Header>
                <h6 className='col-text'>
                    <Fa
                        icon={faExchangeAlt}
                        className='col-players-activity-index'
                    />{' '}
                    {t('html.label.trends30days')}
                </h6>
            </Card.Header>
            <QueryDatapointTable
                id={'playerbase-trends'}
                defaultPreset={'last60Days'}
                filter={{ server: identifier }}
                showTrend
                rows={[
                    {
                        dataType: DatapointType.TOTAL_PLAYERS,
                        color: 'players-count',
                        icon: faUsers,
                        text: t('html.label.totalPlayers'),
                    },
                    {
                        dataType: DatapointType.REGULAR_PLAYERS,
                        color: 'players-regular',
                        icon: faUsers,
                        text: t('html.label.regularPlayers'),
                    },
                    {
                        dataType: DatapointType.PLAYTIME_PER_PLAYER_AVERAGE,
                        color: 'playtime',
                        icon: faClock,
                        text:
                            t('html.label.averagePlaytime') +
                            ' ' +
                            t('html.label.perPlayer'),
                    },
                    {
                        dataType: DatapointType.AFK_TIME_PERCENTAGE,
                        color: 'playtime-afk',
                        icon: faClock,
                        text: t('html.label.afk') + ' ' + t('html.label.perPlayer'),
                    },
                    {
                        dataType: DatapointType.PLAYTIME_PER_REGULAR_PLAYER_AVERAGE,
                        color: 'playtime',
                        icon: faClock,
                        text:
                            t('html.label.averagePlaytime') +
                            ' ' +
                            t('html.label.perRegularPlayer'),
                    },
                    {
                        dataType: DatapointType.SESSION_LENGTH_REGULAR_PLAYER_AVERAGE,
                        color: 'sessions',
                        icon: faClock,
                        text:
                            t('html.label.averageSessionLength') +
                            ' ' +
                            t('html.label.perRegularPlayer'),
                    },
                    {
                        dataType: DatapointType.AFK_TIME_PERCENTAGE_REGULAR_PLAYER_AVERAGE,
                        color: 'playtime-afk',
                        icon: faClock,
                        text:
                            t('html.label.afk') +
                            ' ' +
                            t('html.label.perRegularPlayer'),
                    },
                ]}
            />
        </Card>
    );
};

export default PlayerbaseTrendsCard;
