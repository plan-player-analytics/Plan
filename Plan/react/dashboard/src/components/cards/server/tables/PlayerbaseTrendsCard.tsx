import { useTranslation } from 'react-i18next';
import { Card } from 'react-bootstrap';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { faExchangeAlt, faUsers } from '@fortawesome/free-solid-svg-icons';
import ComparisonTable from '../../../table/ComparisonTable';
import BigTrend from '../../../trend/BigTrend';
import React from 'react';
import { faClock } from '@fortawesome/free-regular-svg-icons';
import { TableRow } from '../../../table/TableRow';
import { CardLoader } from '../../../navigation/Loader';
import FormattedTime from '../../../text/FormattedTime';
import { useTimeAmountFormatter } from '../../../../util/format/useTimeAmountFormatter';
import { Trend } from '../../../../dataHooks/model/datapoint/Trend';

type Props = {
    data?: {
        totalPlayersThen: number;
        totalPlayersNow: number;
        totalPlayersTrend: Trend;
        regularPlayersThen: number;
        regularPlayersNow: number;
        regularPlayersTrend: Trend;
        playtimeAvgThen: number;
        playtimeAvgNow: number;
        playtimeAvgTrend: Trend;
        afkThen: string;
        afkNow: string;
        afkTrend: Trend;
        regularPlaytimeAvgThen: number;
        regularPlaytimeAvgNow: number;
        regularPlaytimeAvgTrend: Trend;
        regularSessionAvgThen: number;
        regularSessionAvgNow: number;
        regularSessionAvgTrend: Trend;
        regularAfkAvgThen: string;
        regularAfkAvgNow: string;
        regularAfkAvgTrend: Trend;
    } | null;
};

const PlayerbaseTrendsCard = ({ data }: Props) => {
    const { t } = useTranslation();
    const { formatTime } = useTimeAmountFormatter();
    if (!data) return <CardLoader />;
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
            <ComparisonTable
                comparisonHeader={t('html.text.comparing30daysAgo')}
                headers={[
                    t('html.label.thirtyDaysAgo'),
                    t('html.label.now'),
                    t('html.label.trend'),
                ]}>
                <TableRow
                    icon={faUsers}
                    color='players-count'
                    text={t('html.label.totalPlayers')}
                    values={[
                        data.totalPlayersThen,
                        data.totalPlayersNow,
                        <BigTrend
                            key={JSON.stringify(data.totalPlayersTrend)}
                            trend={data.totalPlayersTrend}
                        />,
                    ]}
                />
                <TableRow
                    icon={faUsers}
                    color='players-regular'
                    text={t('html.label.regularPlayers')}
                    values={[
                        data.regularPlayersThen,
                        data.regularPlayersNow,
                        <BigTrend
                            key={JSON.stringify(data.regularPlayersTrend)}
                            trend={data.regularPlayersTrend}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='playtime'
                    text={
                        t('html.label.averagePlaytime') +
                        ' ' +
                        t('html.label.perPlayer')
                    }
                    values={[
                        <FormattedTime
                            key={'p-a-t'}
                            timeMs={data.playtimeAvgThen}
                        />,
                        <FormattedTime
                            key={'p-a-n'}
                            timeMs={data.playtimeAvgNow}
                        />,
                        <BigTrend
                            key={JSON.stringify(data.totalPlayersTrend)}
                            trend={data.playtimeAvgTrend}
                            format={(value) => formatTime(Number(value))}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='playtime-afk'
                    text={t('html.label.afk') + ' ' + t('html.label.perPlayer')}
                    values={[
                        data.afkThen,
                        data.afkNow,
                        <BigTrend
                            key={JSON.stringify(data.afkTrend)}
                            trend={data.afkTrend}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='playtime'
                    text={
                        t('html.label.averagePlaytime') +
                        ' ' +
                        t('html.label.perRegularPlayer')
                    }
                    values={[
                        <FormattedTime
                            key={'rg-pl-avg-th'}
                            timeMs={data.regularPlaytimeAvgThen}
                        />,
                        <FormattedTime
                            key={'rg-pl-av-n'}
                            timeMs={data.regularPlaytimeAvgNow}
                        />,
                        <BigTrend
                            key={JSON.stringify(data.regularPlaytimeAvgTrend)}
                            trend={data.regularPlaytimeAvgTrend}
                            format={(value) => formatTime(Number(value))}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='sessions'
                    text={
                        t('html.label.averageSessionLength') +
                        ' ' +
                        t('html.label.perRegularPlayer')
                    }
                    values={[
                        <FormattedTime
                            key={'rg-s-a-t'}
                            timeMs={data.regularSessionAvgThen}
                        />,
                        <FormattedTime
                            key={'rg-s-a-n'}
                            timeMs={data.regularSessionAvgNow}
                        />,
                        <BigTrend
                            key={JSON.stringify(data.regularSessionAvgTrend)}
                            trend={data.regularSessionAvgTrend}
                            format={(value) => formatTime(Number(value))}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='playtime-afk'
                    text={
                        t('html.label.afk') +
                        ' ' +
                        t('html.label.perRegularPlayer')
                    }
                    values={[
                        data.regularAfkAvgThen,
                        data.regularAfkAvgNow,
                        <BigTrend
                            key={JSON.stringify(data.regularAfkAvgTrend)}
                            trend={data.regularAfkAvgTrend}
                        />,
                    ]}
                />
            </ComparisonTable>
        </Card>
    );
};

export default PlayerbaseTrendsCard;
