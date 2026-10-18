import React from 'react';
import { Card, Col } from 'react-bootstrap';
import { FontAwesomeIcon as Fa } from '@fortawesome/react-fontawesome';
import { faCalendarCheck, faClock } from '@fortawesome/free-regular-svg-icons';
import {
    faBookOpen,
    faBraille,
    faCrosshairs,
    faSkull,
} from '@fortawesome/free-solid-svg-icons';
import PunchCard from '../../components/graphs/PunchCard';
import AsNumbersTable from '../../components/table/AsNumbersTable.tsx';
import { usePlayer } from '../layout/PlayerPage';
import { useTranslation } from 'react-i18next';
import PlayerOverviewCard from '../../components/cards/player/PlayerOverviewCard';
import NicknamesCard from '../../components/cards/player/NicknamesCard';
import { TableRow } from '../../components/table/TableRow';
import LoadIn from '../../components/animation/LoadIn.tsx';
import ExtendableRow from '../../components/layout/extension/ExtendableRow.tsx';
import ConnectionsCard from '../../components/cards/player/ConnectionsCard';
import { useAuth } from '../../hooks/authenticationHook.tsx';
import FormattedTime from '../../components/text/FormattedTime.tsx';

const PunchCardCard = ({ player }) => {
    const { t } = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className='col-text'>
                    <Fa icon={faBraille} /> {t('html.label.punchcard')}
                </h6>
            </Card.Header>
            <PunchCard series={player.punchcardSeries} />
        </Card>
    );
};

const OnlineActivityCard = ({ player }) => {
    const { t } = useTranslation();
    return (
        <Card>
            <Card.Header>
                <h6 className='col-text'>
                    <Fa icon={faBookOpen} /> {t('html.label.onlineActivity')}
                </h6>
            </Card.Header>
            <AsNumbersTable
                headers={[
                    t('html.label.last30days'),
                    t('html.label.last7days'),
                ]}>
                <TableRow
                    icon={faClock}
                    color='playtime'
                    text={t('html.label.playtime')}
                    values={[
                        <FormattedTime
                            timeMs={player.onlineActivity.playtime30d}
                        />,
                        <FormattedTime
                            timeMs={player.onlineActivity.playtime7d}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='playtime-active'
                    text={t('html.label.activePlaytime')}
                    values={[
                        <FormattedTime
                            timeMs={player.onlineActivity.activePlaytime30d}
                        />,
                        <FormattedTime
                            timeMs={player.onlineActivity.activePlaytime7d}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='playtime-afk'
                    text={t('html.label.afk')}
                    values={[
                        <FormattedTime
                            timeMs={player.onlineActivity.afkTime30d}
                        />,
                        <FormattedTime
                            timeMs={player.onlineActivity.afkTime7d}
                        />,
                    ]}
                />
                <TableRow
                    icon={faClock}
                    color='sessions'
                    text={t('html.label.medianSessionLength')}
                    values={[
                        <FormattedTime
                            timeMs={
                                player.onlineActivity.medianSessionLength30d
                            }
                        />,
                        <FormattedTime
                            timeMs={player.onlineActivity.medianSessionLength7d}
                        />,
                    ]}
                />
                <TableRow
                    icon={faCalendarCheck}
                    color='sessions'
                    text={t('html.label.sessions')}
                    values={[
                        player.onlineActivity.sessionCount30d,
                        player.onlineActivity.sessionCount7d,
                    ]}
                />
                <TableRow
                    icon={faCrosshairs}
                    color='player-kills'
                    text={t('html.label.playerKills')}
                    values={[
                        player.onlineActivity.playerKillCount30d,
                        player.onlineActivity.playerKillCount7d,
                    ]}
                />
                <TableRow
                    icon={faCrosshairs}
                    color='mob-kills'
                    text={t('html.label.mobKills')}
                    values={[
                        player.onlineActivity.mobKillCount30d,
                        player.onlineActivity.mobKillCount7d,
                    ]}
                />
                <TableRow
                    icon={faSkull}
                    color='deaths'
                    text={t('html.label.deaths')}
                    values={[
                        player.onlineActivity.deathCount30d,
                        player.onlineActivity.deathCount7d,
                    ]}
                />
            </AsNumbersTable>
        </Card>
    );
};

const PlayerOverview = () => {
    const { hasPermission } = useAuth();
    const { player } = usePlayer();

    return (
        <LoadIn>
            {hasPermission('page.player.overview') && (
                <section className='player-overview' id={'player-overview'}>
                    <ExtendableRow id={'row-player-overview-0'}>
                        <Col lg={6}>
                            <PlayerOverviewCard player={player} />
                            <NicknamesCard nicknames={player?.nicknames} />
                            <ConnectionsCard
                                connections={player?.connections}
                            />
                        </Col>
                        <Col lg={6}>
                            <PunchCardCard player={player} />
                            <OnlineActivityCard player={player} />
                        </Col>
                    </ExtendableRow>
                </section>
            )}
        </LoadIn>
    );
};

export default PlayerOverview;
