import React from 'react';
import InsightsFor30DaysCard from '../../common/InsightsFor30DaysCard';
import { useTranslation } from 'react-i18next';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import Datapoint from '../../../datapoint/Datapoint';
import { faLongArrowAltRight, faUser } from '@fortawesome/free-solid-svg-icons';
import SmallTrend from '../../../trend/SmallTrend';
import End from '../../../layout/End';
import ComparingLabel from '../../../trend/ComparingLabel';
import { CardLoader } from '../../../navigation/Loader';
import { Trend } from '../../../../dataHooks/model/datapoint/Trend';

type TwoPlayerChangeProps = {
    colorBefore: string;
    labelBefore: string;
    colorAfter: string;
    labelAfter: string;
};

type Props = {
    data?: {
        newToRegular: number;
        newToRegularTrend: Trend;
        regularToInactive: number;
        regularToInactiveTrend: Trend;
    } | null;
};

const TwoPlayerChange = ({
    colorBefore,
    labelBefore,
    colorAfter,
    labelAfter,
}: TwoPlayerChangeProps) => {
    return (
        <>
            <Fa icon={faUser} className={`col-${colorBefore}`} /> {labelBefore}{' '}
            <Fa icon={faLongArrowAltRight} />{' '}
            <Fa icon={faUser} className={`col-${colorAfter}`} /> {labelAfter}
        </>
    );
};

const PlayerbaseInsightsCard = ({ data }: Props) => {
    const { t } = useTranslation();
    if (!data) return <CardLoader />;
    return (
        <InsightsFor30DaysCard id={'playerbase-insights'}>
            <Datapoint
                name={
                    <TwoPlayerChange
                        colorBefore={'players-new'}
                        labelBefore={t('html.label.new')}
                        colorAfter={'players-regular'}
                        labelAfter={t('html.label.regular')}
                    />
                }
                value={data.newToRegular}
                trend={<SmallTrend trend={data.newToRegularTrend} />}
            />
            <Datapoint
                name={
                    <TwoPlayerChange
                        colorBefore={'players-regular'}
                        labelBefore={t('html.label.regular')}
                        colorAfter={'players-inactive'}
                        labelAfter={t('html.label.inactive')}
                    />
                }
                value={data.regularToInactive}
                trend={<SmallTrend trend={data.regularToInactiveTrend} />}
            />
            <End>
                <ComparingLabel>
                    {t('html.text.comparing30daysAgo')}
                </ComparingLabel>
            </End>
        </InsightsFor30DaysCard>
    );
};

export default PlayerbaseInsightsCard;
