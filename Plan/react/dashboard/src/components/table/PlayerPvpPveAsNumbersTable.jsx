import { useTranslation } from 'react-i18next';
import AsNumbersTable from './AsNumbersTable.tsx';
import { faCrosshairs, faSkull } from '@fortawesome/free-solid-svg-icons';
import React from 'react';
import { TableRow } from './TableRow';

const PlayerPvpPveAsNumbersTable = ({ killData }) => {
    const { t } = useTranslation();
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
                text={t('html.label.kdr')}
                bold
                values={[
                    killData.playerKdrTotal,
                    killData.playerKdr30d,
                    killData.playerKdr7d,
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
                icon={faSkull}
                color='player-kills'
                text={t('html.label.playerDeaths')}
                values={[
                    killData.playerDeathsTotal,
                    killData.playerDeaths30d,
                    killData.playerDeaths7d,
                ]}
            />
            <TableRow
                icon={faCrosshairs}
                color='mob-kills'
                text={t('html.label.mobKdr')}
                bold
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

export default PlayerPvpPveAsNumbersTable;
