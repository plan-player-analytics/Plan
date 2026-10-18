import React from 'react';
import InsightsFor30DaysCard from '../../common/InsightsFor30DaysCard.tsx';
import { useTranslation } from 'react-i18next';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import Datapoint from '../../../datapoint/Datapoint.tsx';
import { faLongArrowAltRight, faUser } from '@fortawesome/free-solid-svg-icons';
import SmallTrend from '../../../trend/SmallTrend';
import End from '../../../layout/End.tsx';
import ComparingLabel from '../../../trend/ComparingLabel.tsx';
import { CardLoader } from '../../../navigation/Loader.tsx';

const TwoPlayerChange = ({
    colorBefore,
    labelBefore,
    colorAfter,
    labelAfter,
}) => {
    return (
        <>
            <Fa icon={faUser} className={`col-${colorBefore}`} /> {labelBefore}{' '}
            <Fa icon={faLongArrowAltRight} />{' '}
            <Fa icon={faUser} className={`col-${colorAfter}`} /> {labelAfter}
        </>
    );
};

const PlayerbaseInsightsCard = ({ data }) => {
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
