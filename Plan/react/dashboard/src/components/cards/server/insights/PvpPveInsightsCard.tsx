import React, { useMemo } from 'react';
import InsightsFor30DaysCard from '../../common/InsightsFor30DaysCard';
import Datapoint from '../../../datapoint/Datapoint';
import { useTranslation } from 'react-i18next';
import { faHandSparkles } from '@fortawesome/free-solid-svg-icons';
import { CardLoader } from '../../../navigation/Loader';
import { useGenericFilter } from '../../../../dataHooks/genericFilterContextHook';
import { useDatapointQuery } from '../../../../dataHooks/useDatapointQuery';
import { DatapointType } from '../../../../dataHooks/model/datapoint/Datapoint';
import { useAuth } from '../../../../hooks/authenticationHook';
import { useDateConverter } from '../../../../util/format/useDateConverter';
import { MS_MONTH } from '../../../../util/format/useDateFormatter';

const PvpPveInsightsCard = () => {
    const { t } = useTranslation();
    const { hasPermission } = useAuth();
    const { after, before, server } = useGenericFilter();
    const { convert } = useDateConverter();

    const allowed = hasPermission('data.server.top.weapons');

    const filter = useMemo(
        () => ({
            after: after ? convert(after).toServerEpochMs() : undefined,
            before: before ? convert(before).toServerEpochMs() : undefined,
            afterMillisAgo: after ? undefined : MS_MONTH,
            server,
        }),
        [after, before, server],
    );

    const { data, isFetching } = useDatapointQuery(
        allowed,
        DatapointType.TOP_WEAPONS,
        filter,
    );

    if (!allowed) return null;
    if (!data || isFetching) return <CardLoader />;

    const weapons = data.value.items ?? [];

    return (
        <InsightsFor30DaysCard id={'pvp-pve-insights'}>
            <Datapoint
                name={t('html.label.deadliestWeapon')}
                icon={faHandSparkles}
                color='top-3-first'
                value={weapons[0] ?? '-'}
            />
            <Datapoint
                name={t('html.label.secondDeadliestWeapon')}
                icon={faHandSparkles}
                color='top-3-second'
                value={weapons[1] ?? '-'}
            />
            <Datapoint
                name={t('html.label.thirdDeadliestWeapon')}
                icon={faHandSparkles}
                color='top-3-third'
                value={weapons[2] ?? '-'}
            />
        </InsightsFor30DaysCard>
    );
};

export default PvpPveInsightsCard;
