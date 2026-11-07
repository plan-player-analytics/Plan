import React from 'react';
import InsightsFor30DaysCard from '../../common/InsightsFor30DaysCard';
import { useTranslation } from 'react-i18next';
import Datapoint from '../../../datapoint/Datapoint';
import { faUserClock, faUserGroup } from '@fortawesome/free-solid-svg-icons';
import SmallTrend from '../../../trend/SmallTrend';
import {
    faCalendar,
    faCalendarPlus,
} from '@fortawesome/free-regular-svg-icons';
import ComparingLabel from '../../../trend/ComparingLabel';
import End from '../../../layout/End';
import { CardLoader } from '../../../navigation/Loader';
import FormattedTime from '../../../text/FormattedTime';
import { useTimeAmountFormatter } from '../../../../util/format/useTimeAmountFormatter';
import { useDecimalFormatter } from '../../../../util/format/useDecimalFormatter';
import { useDataRequest } from '../../../../hooks/dataFetchHook.js';
import { fetchOnlineActivityOverview } from '../../../../service/serverService.js';
import { ErrorViewCard } from '../../../../views/ErrorView';
import { useAuth } from '../../../../hooks/authenticationHook';
import { useParams } from 'react-router';
import { PlanDataResponse } from '../../../../service/PlanResponse';
import { Trend } from '../../../../dataHooks/model/datapoint/Trend';

type OnlineActivityOverview = {
    insights: {
        playersFirstJoinAvg: number;
        playersFirstJoinTrend: Trend;
        firstSessionLengthAvg: number;
        firstSessionLengthTrend: Trend;
        firstSessionLengthMedian: number;
        firstSessionLengthMedianTrend: Trend;
        loneJoins: number;
        loneJoinsTrend: Trend;
        loneNewJoins: number;
        loneNewJoinsTrend: Trend;
    };
};

const OnlineActivityInsightsCard = () => {
    const { t } = useTranslation();
    const { hasPermission } = useAuth();
    const { identifier } = useParams();
    const { formatTime } = useTimeAmountFormatter();
    const { formatDecimals } = useDecimalFormatter();

    const seeOverview = hasPermission('page.server.online.activity.overview');
    const { data, loadingError } = useDataRequest(
        fetchOnlineActivityOverview,
        [identifier],
        seeOverview,
    ) as unknown as PlanDataResponse<OnlineActivityOverview>;

    if (!data) return <CardLoader />;
    if (loadingError) return <ErrorViewCard error={loadingError} />;

    const insights = data.insights;

    return (
        <InsightsFor30DaysCard id={'online-activity-insights'}>
            <Datapoint
                name={t('html.label.onlineOnFirstJoin')}
                icon={faUserGroup}
                color='players-new'
                value={formatDecimals(insights.playersFirstJoinAvg)}
                trend={
                    <SmallTrend
                        trend={insights.playersFirstJoinTrend}
                        format={(value) => formatDecimals(Number(value))}
                    />
                }
            />
            <Datapoint
                name={t('html.label.firstSessionLength.average')}
                icon={faUserClock}
                color='players-new'
                value={
                    <FormattedTime timeMs={insights.firstSessionLengthAvg} />
                }
                trend={
                    <SmallTrend
                        trend={insights.firstSessionLengthTrend}
                        format={(value) => formatTime(Number(value))}
                    />
                }
            />
            <Datapoint
                name={t('html.label.firstSessionLength.median')}
                icon={faUserClock}
                color='players-new'
                value={
                    <FormattedTime timeMs={insights.firstSessionLengthMedian} />
                }
                trend={
                    <SmallTrend
                        trend={insights.firstSessionLengthMedianTrend}
                        format={(value) => formatTime(Number(value))}
                    />
                }
            />
            <Datapoint
                name={t('html.label.loneJoins')}
                icon={faCalendar}
                color='sessions'
                value={insights.loneJoins}
                trend={<SmallTrend trend={insights.loneJoinsTrend} />}
            />
            <Datapoint
                name={t('html.label.loneNewbieJoins')}
                icon={faCalendarPlus}
                color='sessions'
                value={insights.loneNewJoins}
                trend={<SmallTrend trend={insights.loneNewJoinsTrend} />}
            />
            <End>
                <ComparingLabel>
                    {t('html.text.comparing15days')}
                </ComparingLabel>
            </End>
        </InsightsFor30DaysCard>
    );
};

export default OnlineActivityInsightsCard;
