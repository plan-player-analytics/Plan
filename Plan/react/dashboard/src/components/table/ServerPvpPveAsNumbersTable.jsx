import { useTranslation } from 'react-i18next';
import AsNumbersTable from './AsNumbersTable.tsx';
import { faCrosshairs, faSkull } from '@fortawesome/free-solid-svg-icons';
import React from 'react';
import { TableRow } from './TableRow';

const ServerPvpPveAsNumbersTable = ({ killData }) => {
    const { t } = useTranslation();

    if (!killData) return <></>;

    return (
        <AsNumbersTable
            headers={[
                t('html.label.allTime'),
                t('html.label.last30days'),
                t('html.label.last7days'),
            ]}>
            <TableRow
                icon={faCrosshairs}
                color='player-kills'
                text={t('html.label.averageKdr')}
                values={[
                    killData.playerKdrAvg,
                    killData.playerKdrAvg30d,
                    killData.playerKdrAvg7d,
                ]}
            />
            <TableRow
                icon={faCrosshairs}
                color='player-kills'
                text={t('html.label.playerKills')}
                values={[
                    killData.playerKillsTotal,
                    killData.playerKills30d,
                    killData.playerKills7d,
                ]}
            />
            <TableRow
                icon={faCrosshairs}
                color='mob-kills'
                text={t('html.label.averageMobKdr')}
                values={[
                    killData.mobKdrTotal,
                    killData.mobKdr30d,
                    killData.mobKdr7d,
                ]}
            />
            <TableRow
                icon={faCrosshairs}
                color='mob-kills'
                text={t('html.label.mobKills')}
                values={[
                    killData.mobKillsTotal,
                    killData.mobKills30d,
                    killData.mobKills7d,
                ]}
            />
            <TableRow
                icon={faSkull}
                color='mob-kills'
                text={t('html.label.mobDeaths')}
                values={[
                    killData.mobDeathsTotal,
                    killData.mobDeaths30d,
                    killData.mobDeaths7d,
                ]}
            />
            <TableRow
                icon={faSkull}
                color='deaths'
                text={t('html.label.deaths')}
                values={[
                    killData.deathsTotal,
                    killData.deaths30d,
                    killData.deaths7d,
                ]}
            />
        </AsNumbersTable>
    );
};

export default ServerPvpPveAsNumbersTable;
