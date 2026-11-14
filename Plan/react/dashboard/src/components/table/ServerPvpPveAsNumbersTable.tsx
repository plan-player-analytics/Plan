import { useTranslation } from 'react-i18next';
import { faCrosshairs, faSkull } from '@fortawesome/free-solid-svg-icons';
import React from 'react';
import { DatapointType } from '../../dataHooks/model/datapoint/Datapoint';
import { QueryDatapointTable } from './QueryDatapointTable';
import { useParams } from 'react-router';

const ServerPvpPveAsNumbersTable = () => {
    const { t } = useTranslation();
    const { identifier } = useParams();

    return (
        <QueryDatapointTable
            id={'server-pvp-pve-numbers'}
            defaultPreset={'allMonthWeekDay'}
            filter={{ server: identifier }}
            showTrend
            rows={[
                {
                    dataType: DatapointType.KDR_AVERAGE,
                    color: 'player-kills',
                    icon: faCrosshairs,
                    text: t('html.label.averageKdr'),
                },
                {
                    dataType: DatapointType.PLAYER_KILLS,
                    color: 'player-kills',
                    icon: faCrosshairs,
                    text: t('html.label.playerKills'),
                },
                {
                    dataType: DatapointType.MOB_KDR_AVERAGE,
                    color: 'mob-kills',
                    icon: faCrosshairs,
                    text: t('html.label.averageMobKdr'),
                },
                {
                    dataType: DatapointType.MOB_KILLS,
                    color: 'mob-kills',
                    icon: faCrosshairs,
                    text: t('html.label.mobKills'),
                },
                {
                    dataType: DatapointType.MOB_DEATHS,
                    color: 'mob-kills',
                    icon: faSkull,
                    text: t('html.label.mobDeaths'),
                    trendDownGood: true,
                },
                {
                    dataType: DatapointType.DEATHS,
                    color: 'deaths',
                    icon: faSkull,
                    text: t('html.label.deaths'),
                    trendDownGood: true,
                },
            ]}
        />
    );
};

export default ServerPvpPveAsNumbersTable;
